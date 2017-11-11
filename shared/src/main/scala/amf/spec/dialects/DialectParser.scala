package amf.spec.dialects

import amf.compiler.{RamlHeader, Root}
import amf.dialects.{DialectRegistry, DialectValidator, ValidationIssue}
import amf.document.Fragment.DialectFragment
import amf.document.{BaseUnit, Document, Module}
import amf.domain.Annotation._
import amf.domain.dialects.DomainEntity
import amf.domain.{Annotations, Fields}
import amf.metadata.Type
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{Range, YMapOps, YValueOps}
import amf.spec.ParserContext
import amf.spec.common.{ArrayNode, ValueNode}
import amf.spec.declaration.ReferencesParser
import amf.spec.raml.RamlSpecParser
import amf.validation.model.ParserSideValidations
import org.yaml.model._

import scala.collection.mutable

/**
  * Created by Pavel Petrochenko on 12/09/17.
  */
trait DomainEntityVisitor {
  def visit(entity: DomainEntity, prop: DialectPropertyMapping): Boolean
}

class DialectParser(val dialect: Dialect, root: Root)(implicit val ctx: ParserContext) extends RamlSpecParser {

  private var resolver: ReferenceResolver = NullReferenceResolverFactory.resolver(root, Map.empty, ctx)
  // map of references declared within this document
  // references introduced trhough libraries will be handled by the resolver
  private var internalRefs: mutable.HashMap[String, DomainEntity] = mutable.HashMap.empty

  def parseUnit(): BaseUnit = {
    dialect.kind match {
      case ModuleKind   => parseModule
      case FragmentKind => parseFragment
      case DocumentKind => parseDocument
    }
  }

  private def parseDocument = {
    val document = Document().adopted(root.location)
    document.withLocation(root.location)
    document.withEncodes(parseEntity(document))
    document
  }

  private def parseFragment = {
    val fragment = DialectFragment().adopted(root.location)
    fragment.withLocation(root.location)
    fragment.withEncodes(parseEntity(fragment))
    fragment
  }

  private def parseModule = {
    val module = Module().adopted(root.location)
    module.withLocation(root.location)
    module.withDeclares(Seq(parseEntity(module)))
    module
  }

  private def parseEntity(unit: BaseUnit): DomainEntity = {
    val result = root.document.value.map(value => {
      val map = value.toMap

      // This are ALL references, libraries and inclusions
      val references = ReferencesParser("uses", map, root.references).parse()

      resolver = dialect.resolver.resolver(root, references.references.toMap, ctx)

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

  def correctEntityNamespace(node: YValue, domainEntity: DomainEntity) = {
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
      case _ => // ignore
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

    parseNode(entries, entity, topLevel = true)

    dialect.refiner match {
      case Some(dialectRefiner) => dialectRefiner.refine(entity, resolver)
      case None                 => // ignore
    }

    entity
  }

  def validateClosedNode(domainEntity: DomainEntity,
                         entries: YMap,
                         mappings: List[DialectPropertyMapping],
                         topLevel: Boolean): Unit = {
    val entriesLabels = entries.map.keys.map(_.value.toString).toSet
    val entityLabels = if (topLevel) {
      (mappings.map(_.name) ++ Seq("uses", "external")).toSet
    } else {
      mappings.map(_.name).toSet
    }
    val diff = entriesLabels.diff(entityLabels)
    if (diff.nonEmpty) {
      ctx.violation(
        ParserSideValidations.ClosedShapeSpecification.id(),
        domainEntity.id,
        s"Properties: ${diff.mkString(",")} not supported in a ${domainEntity.definition.shortName} node",
        entries
      )
    }
  }

  def parseNodeMapping(mapping: DialectPropertyMapping,
                       entries: YMap,
                       domainEntity: DomainEntity,
                       declaration: Boolean = false): Unit = {
    val entryValue = entries.key(mapping.name)
    entryValue.foreach(entryNode => {
      if (mapping.isMap) {
        parseMap(mapping, entryNode, domainEntity, declaration)
      } else if (mapping.collection) {
        parseCollection(mapping, entryNode, domainEntity)
      } else if (!mapping.isScalar) {
        parseSingleObject(mapping, entryNode, domainEntity)
      } else if (entryNode.value.tag.tagType == YType.Unknown && entryNode.value.tag.text == "!include") {
        resolver
          .resolveToEntity(root, entryNode.value.value.asInstanceOf[YScalar].text, mapping.referenceTarget.get)
          .foreach(child => {
            child.copy(Some(entryNode.key.value.toString)).adopted(domainEntity.id)
            domainEntity.set(mapping.field(), child)
            // parseNode(entryNode.value.value, child)
          })
      } else {
        entryNode.value.value match {
          // in-place definition
          case _: YMap => parseInlineNode(mapping, entryNode, domainEntity)
          // Actual scalar
          case scalar: YScalar => setScalar(domainEntity, mapping, scalar)
        }
      }
    })
    if (entryValue.isEmpty) {
      mapping.defaultValue.foreach(v => {
        domainEntity.set(mapping.field(), v, Annotations() += SynthesizedField())
      })
    }
  }

  def parseNode(node: YValue, domainEntity: DomainEntity, topLevel: Boolean = false): Unit = {
    node match {
      case entries: YMap =>
        correctEntityNamespace(node, domainEntity)
        val mappings = domainEntity.definition.mappings()
        validateClosedNode(domainEntity, entries, mappings, topLevel)
        val declarationMappings  = mappings.filter(_.isDeclaration)
        val encodingDeclarations = mappings.filterNot(_.isDeclaration)
        declarationMappings.foreach { mapping =>
          parseNodeMapping(mapping, entries, domainEntity, declaration = true)
        }
        encodingDeclarations.foreach { mapping =>
          parseNodeMapping(mapping, entries, domainEntity)
        }

      case scalar: YScalar if Option(scalar.value).isDefined =>
        domainEntity.definition.mappings().find(_.fromVal) match {
          case Some(f) => setScalar(domainEntity, f, scalar)
          case None =>
            val name = scalar.value.toString
            internalRefs.get(name) match {
              case Some(internalRef) if internalRef.definition.id == domainEntity.definition.id =>
                internalRef.fields.into(domainEntity.fields)
                domainEntity.annotations += SynthesizedField()
                domainEntity.annotations += DomainElementReference(name, Some(internalRef))
              case _ =>
                resolver.resolveToEntity(root, name, domainEntity.definition) match {
                  case Some(entity) =>
                    entity.fields.into(domainEntity.fields)
                    domainEntity.annotations += SynthesizedField()
                    domainEntity.annotations += DomainElementReference(name, Some(entity))
                  case None =>
                    Some(domainEntity.annotations += DomainElementReference(name, None))
                }
            }
        }
      case _ =>
        ctx.violation(
          ParserSideValidations.DialectExpectingMap.id(),
          domainEntity.id,
          s"Expecting map node or scalar",
          node
        )
      // case _ => throw new MajorParserFailureException(s"Error parsing unknown node $node",node.range)
    }
  }

  private def parseInlineNode(mapping: DialectPropertyMapping,
                              entryNode: YMapEntry,
                              parentDomaineEntity: DomainEntity) = {
    mapping.referenceTarget.foreach(trg => {
      val linkValue = entryNode.key.value match {
        case scalar: YScalar => Some(scalar.text)
        case _               => None
      }

      val child = DomainEntity(linkValue, trg, Fields(), Annotations(entryNode))
      child.adopted(parentDomaineEntity.id)
      parentDomaineEntity.set(mapping.field(), child)
      parseNode(entryNode.value.value, child)
    })
  }

  private def parseMap(mapping: DialectPropertyMapping,
                       entryNode: YMapEntry,
                       parentDomainEntity: DomainEntity,
                       declaration: Boolean): Unit = {
    val targetField = mapping.field()
    entryNode.value.value match {
      case entries: YMap =>
        orderedMap(entries).foreach {
          case (mapKey: YScalar, entry) if mapping.range.isInstanceOf[DialectNode] =>
            val actualRange =
              getActualRange(mapKey.text, mapping, entry, mapping.hash, Some(mapKey.text), Some(parentDomainEntity))

            actualRange match {
              case Some(rangeNode: DialectNode) =>
                val domainEntity = DomainEntity(Some(mapKey.text), rangeNode, Fields(), Annotations(entry))
                if (declaration) { internalRefs.put(mapKey.text, domainEntity) }
                mapping.hash match {
                  case Some(hashProperty) =>
                    domainEntity.set(hashProperty.field(), entryNode.key.value.asInstanceOf[YScalar].text)
                  case None =>
                }
                // order here is important, this will fail, if we do the parseNode before invoking this line
                parentDomainEntity.add(targetField, domainEntity)

                mapping.hashValue match {
                  case Some(hashValue) => entry match {
                    case s: YScalar => domainEntity.set(hashValue.field(), s.text)
                    case _ =>
                  }
                  case None => entry match {
                    case v: YMap                    => parseNode(v, domainEntity)
                    case s: YScalar if s.text != "" => parseNode(s, domainEntity)
                    case _          => // ignore
                  }
                }

                domainEntity.set(mapping.hash.get.field(), mapKey.text)
              case _ => // ignore
            }
        }
      case _ =>
        ctx.violation(
          ParserSideValidations.DialectExpectingMap.id(),
          parentDomainEntity.id,
          Some(mapping.iri()),
          s"Expecting map node for dialect mapping ${mapping.name}, found ${entryNode.value.getClass}",
          entryNode
        )

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
        } else if (mapping.rangeAsDialect.isDefined) {
          var elementCounter = 0
          arr.nodes.foreach { element =>
            val actualRange = getActualRange(null, mapping, element.value, None, None, Some(parentDomainEntity))
            elementCounter += 1
            actualRange match {
              case Some(rangeNode: DialectNode) =>
                val domainEntity =
                  DomainEntity(Some(entryNode.key.value.asInstanceOf[YScalar].text + s"/$elementCounter"),
                               rangeNode,
                               Fields(),
                               Annotations(element))
                val field = mapping.field()
                parentDomainEntity.add(field, domainEntity)
                element.value match {
                  case v: YMap                    => parseNode(v, domainEntity)
                  case s: YScalar if s.text != "" => parseNode(s, domainEntity)
                  case _                          => // ignore
                }
              case _ => // ignore
            }
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
    val value = ArrayNode(arr)
    val array = value.strings()
    val scalars = array.values.map(s => {
      if (s.isInstanceOf[AmfScalar]) {
        s.asInstanceOf[AmfScalar];
      } else {
        AmfScalar(s);
      }
    })
    parentDomainEntity.set(mapping.field(), AmfArray(scalars.map(resolveValue(mapping, _))), Annotations(entryNode))
  }

  private def parseSingleObject(mapping: DialectPropertyMapping,
                                entryNode: YMapEntry,
                                parentDomainEntity: DomainEntity): Option[DomainEntity] = {
    getActualRange(entryNode.key.value.toString, mapping, entryNode.value.value, None, None, None) match {
      case Some(node: DialectNode) =>
        val linkValue = entryNode.key.value match {
          case scalar: YScalar => Some(scalar.text)
          case _               => None
        }
        val domainEntity = DomainEntity(linkValue, node, Fields(), Annotations(entryNode))
        parentDomainEntity.set(mapping.field(), domainEntity)
        parseNode(entryNode.value.value, domainEntity)
        Some(domainEntity)
      case _ => None
    }
  }

  private def shortName(t: Type): String = {
    if (t.isInstanceOf[DialectNode]) {
      t.asInstanceOf[DialectNode].shortName
    } else {
      t.toString
    }
  }
  private def getActualRange(key: String,
                             mapping: DialectPropertyMapping,
                             entryNode: YValue,
                             hash: Option[DialectPropertyMapping],
                             hashValue: Option[String],
                             parentDomainEntity: Option[DomainEntity]): Option[Type] = {
    if (mapping.unionTypes.isDefined && mapping.hashValue.isDefined) {
      ctx.violation(
        ParserSideValidations.ParsingErrorSpecification.id(),
        parentDomainEntity.get.id,
        None,
        s"Multiple range types not supported at the same time that 'hashValue' property",
        parentDomainEntity.get.annotations.find(classOf[LexicalInformation])
      )
      None
    } else if (mapping.unionTypes.isDefined) {

      var nodeId: Option[String] = None

      val types = ctx.validation.disableValidations() { () =>
        mapping.unionTypes.get.map {
          case node: DialectNode =>
            val dialectNode  = node
            val domainEntity = DomainEntity(Option(key), dialectNode, Fields(), Annotations(entryNode))
            try {
              domainEntity.adopted(parentDomainEntity.get.id)
              nodeId = Some(domainEntity.id)
              parseNode(entryNode, domainEntity)
              hash.foreach { hashProperty =>
                domainEntity.set(hashProperty.field(), hashValue.get)
              }
              val issues = DialectValidator.validate(domainEntity)
              (dialectNode, issues)
            } catch {
              case validationException: Exception => {
                (dialectNode,
                 Seq(
                   ValidationIssue(
                     message = validationException.getMessage,
                     entity = domainEntity
                   )))
              }
            }
          case ext: Type => {
            (ext,
             Seq(
               ValidationIssue(
                 message = s"Only a dialect node can be the range of another dialect node, found $ext",
                 entity = parentDomainEntity.get
               )))

            // throw new Exception("")

          }
        }
      }

      if (types.count(r => r._2.isEmpty) > 1) {
        ctx.violation(
          ParserSideValidations.DialectAmbiguousRangeSpecification.id(),
          nodeId.get,
          Some(mapping.iri()),
          s"Ambiguous range for property $key, multiple possible values for range ${types.filter(r => r._2.isEmpty).map(r => shortName(r._1))}",
          entryNode
        )
      }
      if (types.count(r => r._2.isEmpty) == 0) {
        val sb = new mutable.StringBuilder()
        sb.append(s"  Unknown range for property $key, no valid value for range ${mapping.unionTypes.get
          .map(_.asInstanceOf[DialectNode].shortName)}")
        types.foreach {
          case (m, issues) =>
            sb.append(s"\n   Error in range for property $key and mapping ${shortName(m)}")
            issues.foreach { issue =>
              sb.append(s"\n    - ${issue.message}")
            }
        }
        ctx.violation(
          ParserSideValidations.DialectAmbiguousRangeSpecification.id(),
          nodeId.get,
          Some(mapping.iri()),
          sb.mkString,
          entryNode
        )
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
        case _ =>
          val lexical = value.annotations
            .find(classOf[SourceAST])
            .map(_.ast.range)
            .map(range => Range(range))
            .map(LexicalInformation)
          ctx.violation(
            ParserSideValidations.DialectUnresolvableReference.id(),
            value.toString,
            Some(mapping.iri()),
            "Can not resolve reference:" + value.toString,
            lexical
          )
          value
      }
    } else {
      value
    }
  }

  private def setScalar(node: DomainEntity, mapping: DialectPropertyMapping, value: YScalar) = {

    node.set(mapping.field(), resolveValue(mapping, AmfScalar(value.text, Annotations(value))), Annotations(value))
  }

}

object DialectParser {

  def apply(root: Root, header: RamlHeader, dialects: DialectRegistry)(implicit ctx: ParserContext): DialectParser = {
    dialects.get(header.text) match {
      case Some(dialect) => new DialectParser(dialect, root)
      case _             => throw new Exception(s"Unknown dialect ${header.text}")
    }
  }

}
