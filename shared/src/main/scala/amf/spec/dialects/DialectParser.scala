package amf.spec.dialects

import amf.compiler.Root
import amf.dialects.DialectRegistry
import amf.document.Document
import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.Type
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.parser.YMapOps
import amf.spec.common.BaseSpecParser._
import amf.spec.raml.RamlSpecParser
import org.yaml.model._

import scala.collection.mutable.ListBuffer


/**
  * Created by Pavel Petrochenko on 12/09/17.
  */

trait DomainEntityVisitor{
  def visit(entity: DomainEntity, prop: DialectPropertyMapping): Boolean
}

case class DomainEntity(linkValue: Option[String], definition: DialectNode, fields: Fields, annotations: Annotations) extends DomainElement {

  override def adopted(parent: String): this.type = {
    if (Option(this.id).isEmpty) {
      linkValue match {
        case Some(link) =>
          parent.charAt(parent.length - 1) match {
            case '/' => withId(s"$parent$link")
            case '#' => withId(s"$parent$link")
            case _   => withId(s"$parent/$link")
          }
        case _          =>
          withId(parent)
      }
    }
    this
  }

  def traverse(visitor: DomainEntityVisitor): Unit =
      definition.mappings().foreach { mapping =>
        if (!mapping.isScalar) {
          val element = fields.get(mapping.field())
          element match {
            case array: AmfArray => array.values.foreach(visitElement(visitor, mapping, _))
            case _ =>
          }
          visitElement(visitor, mapping, element)
        }
      }

  private def visitElement(visitor: DomainEntityVisitor, mapping: DialectPropertyMapping, element: AmfElement): Unit =
    element match {
      case domainEntity: DomainEntity =>
        val visitChidren = visitor.visit(domainEntity, mapping)
        if (visitChidren) { domainEntity.traverse(visitor) }
      case _ => // ignore
    }

  def boolean(m: DialectPropertyMapping): Option[Boolean] =
    fields.get(m.field()) match {
      case scalar: AmfScalar => Option(scalar.toString.toBoolean)
      case _                 => None
    }

  def string(m: DialectPropertyMapping): Option[String] =
    this.fields.get(m.field()) match {
      case scalar: AmfScalar => Some(scalar.toString)
      case _                 => None
    }

  def addValue(mapping: DialectPropertyMapping, value: String): Unit = add(mapping.field(), AmfScalar(value))


  def strings(m: DialectPropertyMapping): Seq[String] =
    this.fields.get(m.field()) match {
      case scalar: AmfScalar => List(scalar.toString)

      case array: AmfArray   => array.values.map({
          case scalarMember: AmfScalar => Some(scalarMember.toString)
          case _ => None
        }).filter(_.isDefined).map(_.get)

      case _                 => List.empty
    }


  def entity(m: DialectPropertyMapping): Option[DomainEntity] =
    fields.get(m.field()) match {
      case entity: DomainEntity => Some(entity)
      case _                    => None
    }


  def entities(m:DialectPropertyMapping):Seq[DomainEntity] =
    fields.get(m.field()) match {
      case entity: DomainEntity => List(entity)
      case array: AmfArray      => array.values.filter(_.isInstanceOf[DomainEntity]).asInstanceOf[List[DomainEntity]]
      case _                    => List()
    }

  def mapElementWithId(m:DialectPropertyMapping,id:String): Option[DomainEntity] ={
    fields.get(m.field()) match {
      case array: AmfArray =>
        array
          .values
          .filter(v => v.isInstanceOf[DomainEntity])
          .find { case x: DomainEntity => x.id == id }
          .asInstanceOf[Option[DomainEntity]]
      case _ => None
    }
  }

  override def dynamicTypes(): Seq[String] = definition.calcTypes(this).map(_.iri())
}
object DomainEntity{
  def apply(d:DialectNode): DomainEntity = {
    DomainEntity(None,d,Fields(),Annotations())
  }
}

class DialectParser(val dialect: Dialect, override val root: Root) extends RamlSpecParser(root) {

  private val resolver = dialect.resolver.resolver(root)

  def parseDocument(): Document = {
    val dialectDocument = parse()

    Document()
      .adopted(root.location)
      .withEncodes(dialectDocument)
  }

  def parse(): DomainEntity =  root.document.value.map { case entries: YMap =>
    parse(entries)
  }.get

  def parse(entries: YMap): DomainEntity = {
    val entity = DomainEntity(None, dialect.root, Fields(), Annotations(entries))
      .withId(root.location + "#")

    parseNode(entries, entity)

    if (dialect.refiner.isDefined){
      dialect.refiner.get.refine(entity)
    }

    entity
  }

  def parseNode(node: YValue, domainEntity: DomainEntity): Unit = {

    node match {
      case entries: YMap =>
        for {
          id      <- domainEntity.definition.id
          entry   <- entries.key(id)
          value   <- Option(entry.value)
          base    <- Option(ValueNode(value).string().value.toString)
        } yield {
          domainEntity.withId(base)
        }

        domainEntity.definition.mappings().foreach(mapping => {
          entries.key(mapping.name, entryNode => {
            if (mapping.isMap) {
              parseMap(mapping, entryNode, domainEntity)
            } else if (mapping.collection) {
              parseCollection(mapping, entryNode, domainEntity)
            } else if (!mapping.isScalar) {
              parseSingleObject(mapping, entryNode, domainEntity)
            } else {
              parseScalarValue(domainEntity, mapping, entryNode)
            }
          })
        })

      case scalar: YScalar  =>
        domainEntity.definition
          .mappings()
          .find(_.fromVal)
          .foreach(f => {
            setScalar(domainEntity, f, scalar)
          })
    }


  }

  private def parseScalarValue(domainEntity: DomainEntity, mapping: DialectPropertyMapping, entryNode: YMapEntry) = {
    entryNode.value.value match {

      // in-place definition
      case _: YMap =>
        mapping.referenceTarget.foreach(trg => {
          val linkValue = entryNode.key.value match {
            case scalar: YScalar => Some(scalar.text)
            case _               => None
          }

          val child = DomainEntity(linkValue, trg, Fields(), Annotations(entryNode))
          domainEntity.set(mapping.field(), child)
          parseNode(entryNode.value.value, child)
        })


      // Actual scalar
      case scalar: YScalar =>
        setScalar(domainEntity, mapping, scalar)
    }
  }

  private def parseMap(mapping: DialectPropertyMapping, entryNode: YMapEntry, parentDomainEntity: DomainEntity): Unit = {

    entryNode.value.value match {
      case entries: YMap =>
        val classTerms = ListBuffer[DomainEntity]()
        orderedMap(entries).foreach {
          case (classTermName: YScalar, entry) if mapping.range.isInstanceOf[DialectNode] =>
            val domainEntity = DomainEntity(Some(classTermName.text), mapping.range.asInstanceOf[DialectNode], Fields(), Annotations(entry))
            classTerms += domainEntity
            val field = mapping.field()
            parentDomainEntity.add(field, domainEntity)
            domainEntity.set(mapping.hash.get.field(), classTermName.text)
            entry match {
              case v:YMap =>  parseNode(v, domainEntity);
              case s:YScalar =>  parseNode(s, domainEntity);
              case _      =>  // ignore
            }

        }
      case _ => throw new Exception(s"Expecting map node for dialect mapping ${mapping.name}, found ${entryNode.value.getClass}")
    }
  }

  private def orderedMap(entries: YMap) = {
    entries.entries.filter(_.key.value.isInstanceOf[YScalar]).map(e => (e.key.value.asInstanceOf[YScalar], e.value.value))
  }

  private def parseCollection(mapping: DialectPropertyMapping, entryNode: YMapEntry, parentDomainEntity: DomainEntity): Unit = {
    entryNode.value.value match {
      case arr: YSequence =>
        if (mapping.isScalar) {
          mapping.range match {
            case Type.Str =>
              val value = ArrayNode(arr)
              parentDomainEntity.set(mapping.field(), value.strings(), Annotations(entryNode))

            case Type.Iri =>
              val value = ArrayNode(arr)
              val array = value.strings()
              val scalars = array.values.map(AmfScalar(_))
              parentDomainEntity.set(mapping.field(), AmfArray(scalars.map(resolveValue(mapping, _))), Annotations(entryNode))

            case _ => throw new IllegalStateException("Does not know how to parse sequences of other scalars yet")
          }
        } else {
          throw new IllegalStateException("Does not know how to parse sequences of instances yet")
        }

      case _ =>
        if (mapping.isScalar) {
          val resolvedVal = resolveValue(mapping, ValueNode(entryNode.value).string())
          parentDomainEntity.setArray(mapping.field(), Seq(resolvedVal))
        } else {
          throw new IllegalStateException("Does not know how to parse sequences of instances yet")
        }
    }
  }

  private def parseSingleObject(mapping: DialectPropertyMapping, entryNode: YMapEntry, parentDomainEntity: DomainEntity): Unit = {
    mapping.range match {
      case node: DialectNode =>
        val linkValue = entryNode.key.value match {
          case scalar: YScalar => Some(scalar.text)
          case _               => None
        }
        val domainEntity = DomainEntity(linkValue, node, Fields(), Annotations(entryNode))
        parentDomainEntity.set(mapping.field(), domainEntity)
        parseNode(entryNode.value.value, domainEntity)
      case _ => // ignore
    }
  }

  private def resolveValue(mapping: DialectPropertyMapping, value: AmfScalar): AmfScalar = {
    if (mapping.isRef) {
      resolver.resolve(root, value.toString(), mapping.referenceTarget.get) match {
        case Some(finalValue) => AmfScalar(finalValue, value.annotations)
        case _               =>  value
      }

    } else {
      value
    }
  }

  private def setScalar(node: DomainEntity, mapping: DialectPropertyMapping, value: YScalar) =
    node.set(mapping.field(), resolveValue(mapping, AmfScalar(value.text, Annotations(value))), Annotations(value))

}

object DialectParser {

  def apply(root: Root, dialects: DialectRegistry): DialectParser = {
    val dialectDeclaration = s"#${root.parsed.comment.get.metaText}"
    dialects.get(dialectDeclaration) match {
      case Some(dialect) => new DialectParser(dialect,root)
      case _             => throw new Exception(s"Unknown dialect $dialectDeclaration")
    }
  }

}
