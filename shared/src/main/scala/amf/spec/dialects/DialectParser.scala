package amf.spec.dialects

import amf.compiler.{RamlHeader, Root}
import amf.dialects.{DialectRegistry, DialectValidator}
import amf.document.{BaseUnit, Document, Module}
import amf.domain.Annotation.{DomainElementReference, LexicalInformation, NamespaceImportsDeclaration, SynthesizedField}
import amf.domain.dialects.DomainEntity
import amf.domain.{Annotations, Fields}
import amf.metadata.Type
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.spec.raml.RamlSpecParser
import amf.validation.{SeverityLevels, Validation}
import amf.vocabulary.Namespace
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by Pavel Petrochenko on 12/09/17.
  */
trait DomainEntityVisitor {
  def visit(entity: DomainEntity, prop: DialectPropertyMapping): Boolean
}

class DialectParser(val dialect: Dialect, root: Root) extends RamlSpecParser {

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
        unit.withReferences(references.solvedReferences())

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

  def setupEntityMapId(node: YValue, domainEntity: DomainEntity) = {
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
      case _ =>
    }
  }

  def parse(): DomainEntity =
    root.document.value.map {
      case entries: YMap =>
        parse(entries)
    }.get

  def parse(entries: YMap): DomainEntity = {

    val entity = DomainEntity(None, dialect.root, Fields(), Annotations(entries))
    entity.withId(root.location + "#")

    parseNode(entries, entity)

    if (dialect.refiner.isDefined) {
      dialect.refiner.get.refine(entity)
    }

    entity
  }

  def parseNode(node: YValue, domainEntity: DomainEntity): Unit = {
    node match {
      case entries: YMap =>
        setupEntityMapId(node, domainEntity)
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
          val entity = resolver.resolveToEntity(root, nm, domainEntity.definition)
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
          case (mapKey: YScalar, entry) if mapping.range.isInstanceOf[DialectNode] =>
            val actualRange = getActualRange(
              mapKey.text,
              mapping,
              entry,
              mapping.hash,
              Some(mapKey.text),
              Some(parentDomainEntity))

            actualRange match {
              case Some(rangeNode: DialectNode) =>
                val domainEntity = DomainEntity(
                  Some(mapKey.text),
                  rangeNode,
                  Fields(),
                  Annotations(entry))

                mapping.hash.foreach { hashProperty =>
                  domainEntity.set(hashProperty.field(), entryNode.key.value.asInstanceOf[YScalar].text)
                }
                classTerms += domainEntity
                val field = mapping.field()
                parentDomainEntity.add(field, domainEntity)
                domainEntity.set(mapping.hash.get.field(), mapKey.text)
                entry match {
                  case v: YMap    => parseNode(v, domainEntity);
                  case s: YScalar => parseNode(s, domainEntity);
                  case _          => // ignore
                }
              case _ => // ignore
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
    getActualRange(entryNode.key.value.toString, mapping, entryNode.value.value, None, None, None) match {
      case Some(node: DialectNode) =>
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

  private def getActualRange(key: String, mapping: DialectPropertyMapping, entryNode: YValue, hash: Option[DialectPropertyMapping], hashValue: Option[String], parentDomainEntity: Option[DomainEntity]): Option[Type] = {
    if (mapping.unionTypes.isDefined) {

      var nodeId: Option[String] = None

      val types = mapping.unionTypes.get.map {
        case node: DialectNode =>
          val dialectNode  = node
          val domainEntity = DomainEntity(Option(key), dialectNode, Fields(), Annotations(entryNode))
          domainEntity.adopted(parentDomainEntity.get.id)
          nodeId = Some(domainEntity.id)
          parseNode(entryNode, domainEntity)
          hash.foreach { hashProperty =>
            domainEntity.set(hashProperty.field(), hashValue.get)
          }
          val issues = DialectValidator.validate(domainEntity)
          (dialectNode, issues)
        case ext => throw new Exception(s"Only a dialect node can be the range of another dialect node, found $ext")
      }

      if (types.count(r => r._2.isEmpty) > 1) {
        Validation.reportConstraintFailure(
          SeverityLevels.VIOLATION,
          (Namespace.AmfParser + "dialectAmbiguousRange").iri(),
          nodeId.get,
          Some(mapping.iri()),
          s"Ambiguous range for property $key, multiple possible values for range ${types.filter(r => r._2.isEmpty).map(_._1.shortName)}",
          Some(LexicalInformation(amf.parser.Range(entryNode.range))))
      }
      if (types.count(r =>  r._2.isEmpty) == 0) {
        val sb = new mutable.StringBuilder()
        sb.append(s"  Unknown range for property $key, no valid value for range ${mapping.unionTypes.get.map(_.asInstanceOf[DialectNode].shortName)}")
        types.foreach { case (m, issues) =>
          sb.append(s"\n   Error in range for property $key and mapping ${m.shortName}")
          issues.foreach { issue =>
            sb.append(s"\n    - ${issue.message}")
          }
        }
        Validation.reportConstraintFailure(
          SeverityLevels.VIOLATION,
          (Namespace.AmfParser + "dialectAmbiguousRange").iri(),
          nodeId.get,
          Some(mapping.iri()),
          sb.mkString,
          Some(LexicalInformation(amf.parser.Range(entryNode.range))))
      }

      types.find(r => r._2.isEmpty) match {
        case Some((dialectNode, _)) => Some(dialectNode)
        case _                      => None
      }
    } else {
      Some(mapping.range)
    }
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
