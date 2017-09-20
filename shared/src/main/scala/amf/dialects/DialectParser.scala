package amf.dialects

import amf.common.{AMFAST, AMFToken}
import amf.common.AMFToken.SequenceToken
import amf.compiler.Root
import amf.document.Document
import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.Type
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.spec.common.{ArrayNode, Entries, EntryNode, ValueNode}

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
  def apply(d:DialectNode): DomainEntity ={
    new DomainEntity(null,d,Fields(),Annotations());
  }
}

class DialectParser(val dialect: Dialect, val root: Root) {

  private val resolver = dialect.resolver.resolver(root)

  def parseDocument(): Document = {
    val dialectDocument = parse()

    Document()
      .adopted(root.location)
      .withEncodes(dialectDocument)
  }

  def parse(): DomainEntity =  parse(Entries(root.ast.last))

  def parse(entries: Entries): DomainEntity = {
    val entity = DomainEntity(None, dialect.root, Fields(), Annotations(entries.ast))
      .withId(root.location + "#")

    parseNode(entries, entity)

    if (dialect.refiner.isDefined){
      dialect.refiner.get.refine(entity)
    }

    entity
  }

  def parseNode(entries: Entries, domainEntity: DomainEntity): Unit = {
    for {
      id      <- domainEntity.definition.id
      entry   <- entries.key(id)
      value   <- Option(entry.value)
      base    <- Option(ValueNode(value).string().value.toString)
    } yield {
      domainEntity.withId(base)
    }

    if (entries.entries.isEmpty) {
      val valueNode = ValueNode(entries.ast)
      domainEntity.definition
        .mappings()
        .find(_.fromVal)
        .foreach(f => {
          setScalar(domainEntity, f, valueNode)
        })
    } else {
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
    }
  }

  private def parseScalarValue(domainEntity: DomainEntity, mapping: DialectPropertyMapping, entryNode: EntryNode) = {
    if (entryNode.value.is(AMFToken.MapToken) && mapping.allowInplace) {
      mapping.referenceTarget.foreach(trg => {
        val child = DomainEntity(Option(entryNode.key.content), trg, Fields(), Annotations(entryNode.ast))
        domainEntity.set(mapping.field(), child)
        parseNode(Entries(entryNode.value), child)
      })
    }
    else {
      setScalar(domainEntity, mapping, ValueNode(entryNode.value))
    }
  }

  private def parseMap(mapping: DialectPropertyMapping, entryNode: EntryNode, parentDomainEntity: DomainEntity): Unit = {
    val entries = Entries(entryNode.value).entries
    val classTerms = ListBuffer[DomainEntity]()
    entries.foreach {
      case (classTermName, entry) if mapping.range.isInstanceOf[DialectNode] =>
        val domainEntity = DomainEntity(Some(classTermName), mapping.range.asInstanceOf[DialectNode], Fields(), Annotations(entry.ast))
        classTerms += domainEntity
        val field = mapping.field()
        parentDomainEntity.add(field, domainEntity)
        domainEntity.set(mapping.hash.get.field(), classTermName)
        entry.value match {
          case v:AMFAST =>  parseNode(Entries(v), domainEntity);
          case _ =>
        }

    }
  }

  private def parseCollection(mapping: DialectPropertyMapping, entryNode: EntryNode, parentDomainEntity: DomainEntity): Unit = {
    entryNode.value.`type` match {
      case SequenceToken =>
        if (mapping.isScalar) {
          mapping.range match {
            case Type.Str =>
              val value = ArrayNode(entryNode.value)
              parentDomainEntity.set(mapping.field(), value.strings(), entryNode.annotations())

            case Type.Iri =>
              val value = ArrayNode(entryNode.value)
              val array = value.strings()
              val scalars = array.values.map(AmfScalar(_))
              parentDomainEntity.set(mapping.field(), AmfArray(scalars.map(resolveValue(mapping, _))), entryNode.annotations())

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

  private def parseSingleObject(mapping: DialectPropertyMapping, entryNode: EntryNode, parentDomainEntity: DomainEntity): Unit = {
    mapping.range match {
      case node: DialectNode =>
        val domainEntity = DomainEntity(Option(entryNode.key.content), node, Fields(), Annotations(entryNode.ast))
        parentDomainEntity.set(mapping.field(), domainEntity)
        parseNode(Entries(entryNode.value), domainEntity)
      case _ => // ignore
    }
  }

  private def resolveValue(mapping: DialectPropertyMapping, value: AmfScalar): AmfScalar = {
    if (mapping.isRef) {
      resolver.resolve(root, value.toString(), mapping.referenceTarget.get) match {
        case Some(finalValue) => AmfScalar(finalValue)
        case _               =>  value
      }

    } else {
      value
    }
  }

  private def setScalar(node: DomainEntity, mapping: DialectPropertyMapping, value: ValueNode) =
    node.set(mapping.field(), resolveValue(mapping, value.string()))

}

object DialectParser {

  def apply(root: Root, dialects: DialectRegistry): DialectParser = {
    val dialectDeclaration = root.ast.head.content
    dialects.get(dialectDeclaration) match {
      case Some(dialect) => new DialectParser(dialect,root)
      case _             => throw new Exception(s"Unknown dialect $dialectDeclaration")
    }
  }

}
