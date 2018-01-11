package amf.plugins.document.vocabularies.spec

import amf.core.Root
import amf.core.annotations.{ExtendsDialectNode, LexicalInformation, SourceAST, SynthesizedField}
import amf.core.metamodel.Type
import amf.core.model.document.{BaseUnit, Document, Module}
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, Fields, _}
import amf.core.services.RuntimeValidator
import amf.plugins.document.vocabularies.core.{DialectValidator, ValidationIssue}
import amf.plugins.document.vocabularies.model.document.DialectFragment
import amf.plugins.document.vocabularies.model.domain.DomainEntity
import amf.plugins.document.vocabularies.registries.DialectRegistry
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model._

import scala.collection.{immutable, mutable}

/**
  * Parser.
  */
trait DomainEntityVisitor {
  def visit(entity: DomainEntity, prop: DialectPropertyMapping): Boolean
}

class DialectParser(val dialect: Dialect, root: Root)(implicit val ctx: DialectContext) // extends RamlSpecParser
{

  private var resolver: ReferenceResolver = NullReferenceResolverFactory.resolver(root, Map.empty, ctx)
  // map of references declared within this document
  // references introduced through libraries will be handled by the resolver
  private val internalRefs: mutable.HashMap[String, DomainEntity] = mutable.HashMap.empty
  private val possibleRefs: mutable.HashMap[DomainEntity, String] = mutable.HashMap.empty

  def parseUnit(): BaseUnit = {
    dialect.kind match {
      case ModuleKind   => parseModule
      case FragmentKind => parseFragment
      case DocumentKind => parseDocument
    }
  }
  private var isDocument=dialect.kind==DocumentKind;

  private def parseDocument = {
    val document = Document().adopted(root.location)
    document.withLocation(root.location)
    document.withEncodes(parseEntity(document))
    var v = this.internalRefs.values.toList;
    if (!v.isEmpty) {
      document.withDeclares(v)
    }
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
    var v = List(parseEntity(module))
    v = this.internalRefs.values.toList
    module.withDeclares(v)
    module
  }

  private def parseEntity(unit: BaseUnit): DomainEntity = {
    val result = root.parsed.document
      .toOption[YMap]
      .map { map =>
        // This are ALL references, libraries and inclusions
        val references = ReferencesParser("uses", map, root.references).parse(unit.location)

        resolver = dialect.resolver.resolver(root, references.references.toMap, ctx)

        val entity = parse()
        if (possibleRefs.nonEmpty) {
          possibleRefs.foreach { p =>
            internalRefs
              .get(p._2)
              .foreach(referenced => {
                // this is referenced object
                referenced.fields.into(p._1.fields)
                p._1.annotations += SynthesizedField()
                p._1.withLinkTarget(referenced)
                p._1.withLinkLabel(p._2)

              })
          }
        }
        if (references.references.nonEmpty) {
          unit.withReferences(references.solvedReferences())

        }
        entity
      }

    result match {
      case Some(e) => e
      case _       => throw new Exception("Empty document.")
    }
  }

  def correctEntityNamespace(node: YNode, domainEntity: DomainEntity): Any = {
    node.to[YMap] match {
      case Right(map) =>
        for {
          id    <- domainEntity.definition.id
          entry <- map.key(id)
          value <- Option(entry.value)
          base  <- Option(ValueNode(value).string().value.toString)
        } yield {
          domainEntity.withId(base)
        }
      case _ => // ignore
    }
  }

  def parse(): DomainEntity = parse(root.parsed.document.as[YMap])

  def parse(entries: YMap): DomainEntity = {

    val entity = DomainEntity(None, dialect.root, Fields(), Annotations(entries))
    entity.withId(root.location + "#")

    parseNode(entries, entity, topLevel = true)

    dialect.jsonLDRefiner match {
      case Some(dialectRefiner) => dialectRefiner.refine(entity, resolver)
      case None                 => // ignore
    }

    entity
  }

  def validateClosedNode(domainEntity: DomainEntity,
                         entries: YMap,
                         mappings: List[DialectPropertyMapping],
                         topLevel: Boolean): Unit = {
    val entriesLabels = entries.map.keys.map(_.value.toString).toSet-"";
    val entityLabels = if (topLevel) {
      (mappings.map(_.name) ++ Seq("uses", "external","!extends")).toSet
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
    entryValue.foreach { entryNode =>
      if (mapping.isMap) {
        parseMap(mapping, entryNode, domainEntity, declaration)
      } else if (mapping.collection) {
        parseCollection(mapping, entryNode, domainEntity)
      } else if (!mapping.isScalar) {
        parseSingleObject(mapping, entryNode, domainEntity)
      } else if (entryNode.value.tagType == YType.Include) {
        resolver
          .resolveToEntity(root, entryNode.value.as[YScalar].text, mapping.referenceTarget.get)
          .foreach { child =>
            if (mapping.isRef) {
              if (child.isLink) {
                domainEntity.set(mapping.field(), child.linkTarget.get.id)
              } else domainEntity.set(mapping.field(), child.id)
            } else {
              val lnk = child.link[DomainEntity](entryNode.value.value.asInstanceOf[YScalar].text)
              // child.copy(Some(entryNode.key.value.toString)).adopted(domainEntity.id)
              domainEntity.set(mapping.field(), lnk)
              // parseNode(entryNode.value.value, child)
            }
          }
      } else {
        entryNode.value.tagType match {
          // in-place definition
          case YType.Map => parseInlineNode(mapping, entryNode, domainEntity)
          // Actual scalar
          case _ if entryNode.value.toOption[YScalar].isDefined =>
            setScalar(domainEntity, mapping, entryNode.value.as[YScalar])
        }
      }
    }
    if (entryValue.isEmpty) {
      mapping.defaultValue.foreach(v => {
        domainEntity.set(mapping.field(), v, Annotations() += SynthesizedField())
      })
    }
  }

  def parseNode(node: YNode, domainEntity: DomainEntity, topLevel: Boolean = false): Unit = {
    node.tagType match {
      case YType.Map =>
        correctEntityNamespace(node, domainEntity)
        val mappings = domainEntity.definition.mappings()
        val map      = node.as[YMap]
        var extended=node.as[YMap].entries.find(v=>v.key.tag.text=="!extends");
        if (extended.isDefined){
          extended.get.value.value match {
            case s:YScalar => {
              val ref=s.text;
              var extendedEntity=resolver.resolveToEntity(root,ref,domainEntity.definition);
              if (extendedEntity.isDefined){
                domainEntity.annotations.+=(ExtendsDialectNode(ref))
                extendedEntity.get.fields.into(domainEntity.fields);
                // do we need to mark source of extension.
                // yes
              }
              else{
                ctx.violation(
                  ParserSideValidations.DialectExtendIssue.id(),
                  domainEntity.id,
                  s"Extending unknown node " + ref,
                  node
                )
              }
            }
            case _=>  ctx.violation(
              ParserSideValidations.DialectExtendIssue.id(),
              domainEntity.id,
              s"Expecting scalar in !extends",
              node
            )
          }
        }
        validateClosedNode(domainEntity, map, mappings, topLevel)
        val declarationMappings  = mappings.filter(if (this.isDocument) _.isDocumentDeclaration else _.isDeclaration)
        val encodingDeclarations = mappings.filterNot((if (this.isDocument) _.isDocumentDeclaration else _.isDeclaration))
        declarationMappings.foreach { mapping =>
          parseNodeMapping(mapping, map, domainEntity, declaration = true)
        }
        encodingDeclarations.foreach { mapping =>
          parseNodeMapping(mapping, map, domainEntity)
        }

      case _ if node.toOption[YScalar].map(_.text).isDefined =>
        val scalar = node.as[YScalar]
        domainEntity.definition.mappings().find(_.fromVal) match {
          case Some(f) => setScalar(domainEntity, f, scalar)
          case None =>
            val name = scalar.toString()
            internalRefs.get(name) match {
              case Some(internalRef) if internalRef.definition.id == domainEntity.definition.id =>
                internalRef.fields.into(domainEntity.fields)
                domainEntity.annotations += SynthesizedField()
                domainEntity.withLinkTarget(internalRef)
                domainEntity.withLinkLabel(name)
              // domainEntity.annotations += DomainElementReference(name, Some(internalRef))
              case _ =>
                resolver.resolveToEntity(root, name, domainEntity.definition) match {
                  case Some(entity) =>
                    entity.fields.into(domainEntity.fields)
                    domainEntity.annotations += SynthesizedField()
                  // domainEntity.annotations += DomainElementReference(name, Some(entity))
                  case None =>
                    // this is possible internal reference;
                    possibleRefs.put(domainEntity, name);
                  // Some(domainEntity.annotations += DomainElementReference(name, None))
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
                              parentDomaineEntity: DomainEntity): Unit = {
    mapping.referenceTarget.foreach(trg => {
      val linkValue = entryNode.key.value match {
        case scalar: YScalar => Some(scalar.text)
        case _               => None
      }

      val child = DomainEntity(linkValue, trg, Fields(), Annotations(entryNode))
      child.adopted(parentDomaineEntity.id)
      parentDomaineEntity.set(mapping.field(), child)
      parseNode(entryNode.value, child)
    })
  }

  private def parseMap(mapping: DialectPropertyMapping,
                       entryNode: YMapEntry,
                       parentDomainEntity: DomainEntity,
                       declaration: Boolean): Unit = {
    val targetField = mapping.field()
    entryNode.value.to[YMap] match {
      case Right(entries) =>
        orderedMap(entries).foreach {
          case (mapKey: YScalar, entry) if mapping.range.isInstanceOf[DialectNode] =>
            val actualRange =
              getActualRange(Option(mapKey.text),
                             mapping,
                             entry,
                             mapping.hash,
                             Some(mapKey.text),
                             Some(parentDomainEntity))

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
                  case Some(hashValue) =>
                    entry match {
                      case _ if entry.toOption[YScalar].isDefined =>
                        domainEntity.set(hashValue.field(), entry.as[YScalar].text)
                      case _ =>
                    }
                  case None =>
                    entry.tagType match {
                      case YType.Map => parseNode(entry.as[YMap], domainEntity)
                      case _ if entry.toOption[YScalar].exists(t => Option(t.text).isDefined && !t.text.equals("")) =>
                        parseNode(entry, domainEntity)
                      case _ => // ignore}
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

  private def orderedMap(entries: YMap): IndexedSeq[(YScalar, YNode)] = {
    entries.entries
      .filter(_.key.toOption[YScalar].isDefined)
      .map(e => (e.key.as[YScalar], e.value))
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
            val actualRange = getActualRange(None, mapping, element, None, None, Some(parentDomainEntity))
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
                element.tagType match {
                  case YType.Map => parseNode(element.as[YMap], domainEntity)
                  case _ if element.toOption[YScalar].exists(t => Option(t.text).isDefined && !t.text.equals("")) =>
                    parseNode(element, domainEntity)
                  case _ => // ignore
                }
              case _ =>
                ctx.violation(
                  ParserSideValidations.ParsingErrorSpecification.id(),
                  parentDomainEntity.id,
                  Some(mapping.iri()),
                  s"Can not determine actual range of the node",
                  parentDomainEntity.annotations.find(classOf[LexicalInformation])
                )
            }
          }
        }
      case _ =>
        if (mapping.isScalar) {
          if (entryNode.value.value.isInstanceOf[YScalar]) {
            val scalar = ValueNode(entryNode.value).string()
            if (Option(scalar.value).isDefined) {
              val resolvedVal = resolveValue(mapping, scalar, parentDomainEntity)
              parentDomainEntity.setArray(mapping.field(), Seq(resolvedVal))
            }
          } else {
            if (entryNode.value.value.isInstanceOf[YMap] && mapping.allowInplace) {
              mapping.referenceTarget.foreach(trg => {
                val child =
                  DomainEntity(Option(entryNode.key.value.toString), trg, Fields(), Annotations(entryNode.value.value))
                parentDomainEntity.add(mapping.field(), child)
                parseNode(entryNode.value, child)
              })
            }
          }
        } else {
          ctx.violation(
            ParserSideValidations.ParsingErrorSpecification.id(),
            parentDomainEntity.id,
            Some(mapping.iri()),
            s"Expecting sequence of nodes",
            parentDomainEntity.annotations.find(classOf[LexicalInformation])
          )
        }

    }
  }

  private def parseArrayRefs(mapping: DialectPropertyMapping,
                             entryNode: YMapEntry,
                             parentDomainEntity: DomainEntity,
                             arr: YSequence) = {
    val value = ArrayNode(arr)
    val array = value.strings()
    val scalars = array.values.map {
      case scalar: AmfScalar =>
        scalar
      case s =>
        AmfScalar(s)
    }
    parentDomainEntity.set(mapping.field(),
                           AmfArray(scalars.map(resolveValue(mapping, _, parentDomainEntity))),
                           Annotations(entryNode))
  }

  private def parseSingleObject(mapping: DialectPropertyMapping,
                                entryNode: YMapEntry,
                                parentDomainEntity: DomainEntity): Option[DomainEntity] = {
    getActualRange(entryNode.key.toOption[YScalar].map(_.text), mapping, entryNode.value, None, None, None) match {
      case Some(node: DialectNode) =>
        val linkValue = entryNode.key.value match {
          case scalar: YScalar => Some(scalar.text)
          case _               => None
        }
        val domainEntity = DomainEntity(linkValue, node, Fields(), Annotations(entryNode))
        parentDomainEntity.set(mapping.field(), domainEntity)
        parseNode(entryNode.value, domainEntity)
        Some(domainEntity)
      case _ => None
    }
  }

  private def shortName(t: Type): String = {
    t match {
      case node: DialectNode =>
        node.shortName
      case _ =>
        t.toString
    }
  }
  private def getActualRange(key: Option[String],
                             mapping: DialectPropertyMapping,
                             entryNode: YNode,
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

      val types: immutable.Seq[(Type, Seq[ValidationIssue])] = RuntimeValidator.disableValidations() { () =>
        mapping.unionTypes.get.map {
          case node: DialectNode =>
            val dialectNode  = node
            val domainEntity = DomainEntity(key, dialectNode, Fields(), Annotations(entryNode))
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
              case validationException: Exception =>
                (dialectNode,
                 Seq(
                   ValidationIssue(
                     message = validationException.getMessage,
                     entity = domainEntity
                   )))
            }
          case ext: Type =>
            (ext,
             Seq(
               ValidationIssue(
                 message = s"Only a dialect node can be the range of another dialect node, found $ext",
                 entity = parentDomainEntity.get
               )))

          // throw new Exception("")
        }
      }

      if (types.count(r => r._2.isEmpty) > 1) {
        ctx.violation(
          ParserSideValidations.DialectAmbiguousRangeSpecification.id(),
          nodeId.get,
          Some(mapping.iri()),
          s"Ambiguous range for property ${key
            .getOrElse("")}, multiple possible values for range ${types.filter(r => r._2.isEmpty).map(r => shortName(r._1))}",
          entryNode
        )
      }
      if (types.count(r => r._2.isEmpty) == 0) {
        val sb = new mutable.StringBuilder()
        sb.append(
          s"  Unknown range for property ${key.getOrElse("")}, no valid value for range ${mapping.unionTypes.get
            .map(_.asInstanceOf[DialectNode].shortName)}")
        types.foreach {
          case (m, issues) =>
            sb.append(s"\n   Error in range for property ${key.getOrElse("")} and mapping ${shortName(m)}")
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

  private def resolveValue(mapping: DialectPropertyMapping, value: AmfScalar, parent: DomainEntity): AmfScalar = {
    if (mapping.isRef) {
      resolver.resolve(root, value.toString(), mapping.referenceTarget.get) match {
        case Some(finalValue) => AmfScalar(finalValue, value.annotations)
        case _ =>
          val lexical = value.annotations
            .find(classOf[SourceAST])
            .map(_.ast.range)
            .map(range => Range(range))
            .map(range => LexicalInformation(range))
          ctx.violation(
            ParserSideValidations.DialectUnresolvableReference.id(),
            parent.id,
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

    node.set(mapping.field(),
             resolveValue(mapping, AmfScalar(value.text, Annotations(value)), node),
             Annotations(value))
  }

}

object DialectParser {

  def apply(root: Root, header: String, dialects: DialectRegistry)(implicit ctx: DialectContext): DialectParser = {
    dialects.get(header) match {
      case Some(dialect) => new DialectParser(dialect, root)
      case _             => throw new Exception(s"Unknown dialect $header")
    }
  }

}
