package amf.spec.dialects

import amf.compiler.{RamlHeader, Root}
import amf.dialects.{DialectRegistry, DialectValidator}
import amf.document.{BaseUnit, Document, Module}
import amf.domain.Annotation.{DomainElementReference, NamespaceImportsDeclaration, SynthesizedField}
import amf.domain.dialects.DomainEntity
import amf.domain.{Annotations, Fields}
import amf.metadata.Type
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.spec.raml.RamlSpecParser
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by Pavel Petrochenko on 12/09/17.
  */
trait DomainEntityVisitor {
  def visit(entity: DomainEntity, prop: DialectPropertyMapping): Boolean
}

class DialectParser(val dialect: Dialect, override val root: Root) extends RamlSpecParser(root) {

  private var resolver: ReferenceResolver = NullReferenceResolverFactory.resolver(root, Map.empty)

  def parseUnit(): BaseUnit = {
    dialect.kind match {
      case ModuleKind   => parseModule
      case FragmentKind => parseFragment
      case DocumentKind => parseDocument
    }
  }

  private def parseDocument = {
    val document = Document().adopted(root.location)
    document.withEncodes(parseEntity(document))
    document
  }

  private def parseFragment = {
    val fragment = DialectFragment().adopted(root.location)
    fragment.withEncodes(parseEntity(fragment))
    fragment
  }

  private def parseModule = {
    val module = Module().adopted(root.location)
    module.withDeclares(Seq(parseEntity(module)))
    module
  }

  private def parseEntity(unit: BaseUnit): DomainEntity = {
    val result = root.document.value.map(value => {
      val map = value.toMap

      val references = ReferencesParser("uses", map, root.references).parse()

      resolver = dialect.resolver.resolver(root, references.references.toMap)

      val entity = parse()

      if (references.references.nonEmpty) {
        unit.withReferences(references.references.values.toSeq)

        val usesMap: mutable.Map[String, String] = mutable.Map()
        map.key(
          "uses",
          entry =>
            entry.value.value.toMap.entries.foreach(e => {
              usesMap.put(e.key.value.toScalar.text, e.value.value.toScalar.text)
            })
        )
        entity.annotations += NamespaceImportsDeclaration(usesMap.toMap)
      }
      entity
    })

    result match {
      case Some(e) => e
      case _       => throw new Exception("Empty document.")
    }
  }

  def parse(): DomainEntity =
    root.document.value.map {
      case entries: YMap =>
        parse(entries)
    }.get

  def parse(entries: YMap): DomainEntity = {

    val entity = DomainEntity(None, dialect.root, Fields(), Annotations(entries))
      .withId(root.location + "#")

    parseNode(entries, entity)

    if (dialect.refiner.isDefined) {
      dialect.refiner.get.refine(entity)
    }

    entity
  }

  def parseNode(node: YValue, domainEntity: DomainEntity): Unit = {
    node match {
      case entries: YMap =>
        for {
          id    <- domainEntity.definition.id
          entry <- entries.key(id)
          value <- Option(entry.value)
          base  <- Option(ValueNode(value).string().value.toString)
        } yield {
          domainEntity.withId(base)
        }

        domainEntity.definition
          .mappings()
          .foreach(mapping => {
            val ev = entries.key(mapping.name)
            ev.foreach(entryNode => {
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
            if (ev.isEmpty) {
              mapping.defaultValue.foreach(v => {
                domainEntity.set(mapping.field(), v, Annotations() += SynthesizedField())
              })
            }
          })

      case scalar: YScalar =>
        val maybeMapping = domainEntity.definition
          .mappings()
          .find(_.fromVal)
        maybeMapping
          .foreach(f => {
            setScalar(domainEntity, f, scalar)
          })
        if (maybeMapping.isEmpty) {
          val nm     = scalar.value.toString
          val entity = resolver.resolveToEndity(root, nm, domainEntity.definition)
          entity.foreach(e => {
            e.fields.into(domainEntity.fields)
            domainEntity.annotations += SynthesizedField()
            domainEntity.annotations += DomainElementReference(nm, Some(e))
          })
          entity.orElse(Some(domainEntity.annotations += DomainElementReference(nm, None)))
        }
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
            val domainEntity = DomainEntity(Some(classTermName.text),
                                            getActualRange(mapping, entry).asInstanceOf[DialectNode],
                                            Fields(),
                                            Annotations(entry))
            classTerms += domainEntity
            val field = mapping.field()
            parentDomainEntity.add(field, domainEntity)
            domainEntity.set(mapping.hash.get.field(), classTermName.text)
            entry match {
              case v: YMap    => parseNode(v, domainEntity);
              case s: YScalar => parseNode(s, domainEntity);
              case _          => // ignore
            }

        }
      case _ =>
        throw new Exception(
          s"Expecting map node for dialect mapping ${mapping.name}, found ${entryNode.value.getClass}")
    }
  }

  private def orderedMap(entries: YMap) = {
    entries.entries
      .filter(_.key.value.isInstanceOf[YScalar])
      .map(e => (e.key.value.asInstanceOf[YScalar], e.value.value))
  }

  private def parseCollection(mapping: DialectPropertyMapping,
                              entryNode: YMapEntry,
                              parentDomainEntity: DomainEntity): Unit = {
    entryNode.value.value match {
      case arr: YSequence =>
        if (mapping.isScalar) {

          mapping.range match {
            case Type.Str =>
              if (mapping.isRef) {
                parseArrayRefs(mapping, entryNode, parentDomainEntity, arr)
              } else {
                val value = ArrayNode(arr)
                parentDomainEntity.set(mapping.field(), value.strings(), Annotations(entryNode))
              }
            case Type.Iri =>
              parseArrayRefs(mapping, entryNode, parentDomainEntity, arr)

            case _ => throw new IllegalStateException("Does not know how to parse sequences of other scalars yet")
          }
        } else {
          throw new IllegalStateException("Does not know how to parse sequences of instances yet")
        }

      case _ =>
        if (mapping.isScalar) {
          if (entryNode.value.value.isInstanceOf[YScalar]) {
            val scalar = ValueNode(entryNode.value).string()
            if (Option(scalar.value).isDefined) {
              val resolvedVal = resolveValue(mapping, scalar)
              parentDomainEntity.setArray(mapping.field(), Seq(resolvedVal))
            }
          } else {
            if (entryNode.value.value.isInstanceOf[YMap] && mapping.allowInplace) {
              mapping.referenceTarget.foreach(trg => {
                val child =
                  DomainEntity(Option(entryNode.key.value.toString), trg, Fields(), Annotations(entryNode.value.value))
                parentDomainEntity.add(mapping.field(), child)
                parseNode(entryNode.value.value, child)
              })
            }
          }
        } else throw new IllegalStateException("Does not know how to parse sequences of instances yet")

    }
  }

  private def parseArrayRefs(mapping: DialectPropertyMapping,
                             entryNode: YMapEntry,
                             parentDomainEntity: DomainEntity,
                             arr: YSequence) = {
    val value   = ArrayNode(arr)
    val array   = value.strings()
    val scalars = array.values.map(AmfScalar(_))
    parentDomainEntity.set(mapping.field(), AmfArray(scalars.map(resolveValue(mapping, _))), Annotations(entryNode))
  }

  private def parseSingleObject(mapping: DialectPropertyMapping,
                                entryNode: YMapEntry,
                                parentDomainEntity: DomainEntity): Unit = {
    getActualRange(mapping, entryNode.value.value) match {
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

  private def getActualRange(mapping: DialectPropertyMapping, entryNode: YValue): Type = {
    if (mapping.unionTypes.isDefined) {
      val maybeType = mapping.unionTypes.get.find {
        case node: DialectNode =>
          var dl           = node
          val domainEntity = DomainEntity(Option("#"), dl, Fields(), Annotations(entryNode))
          domainEntity.withId("#")
          parseNode(entryNode, domainEntity)
          val issues = DialectValidator.validate(domainEntity)
          issues.isEmpty
        case _ => false
      }
      if (maybeType.isDefined) {
        maybeType.get
      } else mapping.range
    } else
      mapping.range
  }

  private def resolveValue(mapping: DialectPropertyMapping, value: AmfScalar): AmfScalar = {
    if (mapping.isRef) {
      resolver.resolve(root, value.toString(), mapping.referenceTarget.get) match {
        case Some(finalValue) => AmfScalar(finalValue, value.annotations)
        case _                => value
      }

    } else {
      value
    }
  }

  private def setScalar(node: DomainEntity, mapping: DialectPropertyMapping, value: YScalar) =
    node.set(mapping.field(), resolveValue(mapping, AmfScalar(value.text, Annotations(value))), Annotations(value))

}

object DialectParser {

  def apply(root: Root, header: RamlHeader, dialects: DialectRegistry): DialectParser = {
    dialects.get(header.text) match {
      case Some(dialect) => new DialectParser(dialect, root)
      case _             => throw new Exception(s"Unknown dialect ${header.text}")
    }
  }

}
