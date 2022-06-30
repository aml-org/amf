package amf.apicontract.internal.spec.raml.parser.document

import amf.aml.internal.parse.common.DeclarationKey
import amf.apicontract.client.scala.model.document.{APIContractProcessingData, Extension, Overlay}
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.client.scala.model.domain.{EndPoint, Response, Tag}
import amf.apicontract.internal.annotations.ExtendsReference
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.metamodel.domain.security.SecuritySchemeModel
import amf.apicontract.internal.metamodel.domain.templates.{ResourceTypeModel, TraitModel}
import amf.apicontract.internal.spec.common.parser.{WebApiShapeParserContextAdapter, _}
import amf.apicontract.internal.spec.common.{OasParameter, RamlWebApiDeclarations, WebApiDeclarations}
import amf.apicontract.internal.spec.oas.parser.domain.{
  LicenseParser,
  OasResponseParser,
  OrganizationParser,
  TagsParser
}
import amf.apicontract.internal.spec.raml.parser.context.{ExtensionLikeWebApiContext, RamlWebApiContext}
import amf.apicontract.internal.spec.raml.parser.document.RamlAnnotationTargets.targetsFor
import amf.apicontract.internal.spec.spec.toOas
import amf.apicontract.internal.validation.definitions.ParserSideValidations._
import amf.core.client.scala.model.document.{BaseUnit, Document, ExtensionLike, Module}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfArray, AmfObject, AmfScalar}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.annotations._
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.document.{BaseUnitModel, DocumentModel, ExtensionLikeModel}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.internal.parser.domain.{Annotations, ArrayNode, ScalarNode, SearchScope}
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}
import amf.core.internal.remote.Spec
import amf.core.internal.utils._
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.client.scala.model.domain.CreativeWork
import amf.shapes.internal.spec.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser.{AnnotationParser, RamlCreativeWorkParser, RamlScalarNode, YMapEntryLike}
import amf.shapes.internal.spec.raml.parser.{Raml10TypeParser, RamlTypeEntryParser, RamlTypeSyntax, StringDefaultType}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  ExclusiveSchemasType,
  InvalidFragmentType,
  InvalidTypeDefinition
}
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** Extension and Overlay parser
  */
case class ExtensionLikeParser(root: Root, spec: Spec)(implicit override val ctx: ExtensionLikeWebApiContext)
    extends RamlDocumentParser(root, spec)
    with Raml10BaseSpecParser {

  def parseExtension(): Extension = {
    ctx.contextType = RamlWebApiContextType.EXTENSION

    getParent match {
      case Some(parent) => collectAncestorsDeclarationsAndReferences(parent, ctx.parentDeclarations)
      case _            => // nothing to do
    }

    val extension = parseDocument(Extension())

    parseExtension(extension, ExtensionLikeModel.Extends)

    extension
  }

  def parseOverlay(): Overlay = {
    ctx.contextType = RamlWebApiContextType.OVERLAY

    getParent match {
      case Some(parent) => collectAncestorsDeclarationsAndReferences(parent, ctx.parentDeclarations)
      case _            => // nothing to do
    }

    val overlay = parseDocument(Overlay())

    parseExtension(overlay, ExtensionLikeModel.Extends)

    overlay
  }

  private def getParent: Option[BaseUnit] = {
    root.references.map(_.unit).collectFirst { case u @ (_: ExtensionLike[_] | _: Document) => u }
  }

  private def collectAncestorsDeclarationsAndReferences(
      reference: BaseUnit,
      collector: RamlWebApiDeclarations
  ): Unit = {

    reference.asInstanceOf[Document].declares.foreach(collector += _)

    val (exLikeOrDocument, otherReferences) = reference.references.partition({
      case _: ExtensionLike[_] | _: Document => true
      case _                                 => false
    })

    reference.annotations
      .find(classOf[Aliases])
      .foreach(_.aliases.foreach { alias =>
        val fullUrl = alias._2.fullUrl
        otherReferences.find(_.location().exists(_.equals(fullUrl))) match {
          case Some(library: Module) =>
            val libraryDeclarations = collector.getOrCreateLibrary(alias._1)
            library.declares.foreach(libraryDeclarations += _)
          case _ => // nothing to do
        }
      })

    exLikeOrDocument.foreach(collectAncestorsDeclarationsAndReferences(_, collector))
  }

  private def parseExtension(document: ExtensionLike[_], field: Field): Unit = {
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

    UsageParser(map, document).parse()

    map
      .key("extends")
      .foreach(e => {
        root.references
          .find(_.origin.url == e.value.as[String])
          .foreach { extend =>
            document.callAfterAdoption { () =>
              document.setWithoutId(
                field,
                AmfScalar(extend.unit.id, Annotations(e.value)),
                Annotations(e) += ExtendsReference(extend.origin.url)
              )
            }
          }
      })
  }
}

object ExtensionLikeParser {
  def apply(root: Root, spec: Spec, baseCtx: RamlWebApiContext): ExtensionLikeParser = {
    val parentDeclarations = new RamlWebApiDeclarations(
      alias = None,
      errorHandler = baseCtx.eh,
      futureDeclarations = baseCtx.futureDeclarations
    )
    val exLikeCtx: ExtensionLikeWebApiContext = new ExtensionLikeWebApiContext(
      baseCtx.rootContextDocument,
      baseCtx.refs,
      baseCtx,
      Some(baseCtx.declarations),
      parentDeclarations,
      options = baseCtx.options
    )
    new ExtensionLikeParser(root, spec)(exLikeCtx)
  }
}

/** Raml 1.0 spec parser
  */
case class Raml10DocumentParser(root: Root)(implicit override val ctx: RamlWebApiContext)
    extends RamlDocumentParser(root, Spec.RAML10)
    with Raml10BaseSpecParser {}

abstract class RamlDocumentParser(root: Root, spec: Spec)(implicit val ctx: RamlWebApiContext)
    extends RamlBaseDocumentParser {

  def parseDocument[T <: Document](document: T): T = {
    document.withLocation(root.location).withProcessingData(APIContractProcessingData().withSourceSpec(spec))

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

    val references = ReferencesParser(document, root.location, "uses", map, root.references).parse()
    parseDeclarations(root, map)
    val api = parseWebApi(map)
    document.setWithoutId(DocumentModel.Encodes, api, Annotations.inferred())

    addDeclarationsToModel(document)
    if (references.nonEmpty)
      document.setWithoutId(
        DocumentModel.References,
        AmfArray(references.baseUnitReferences()),
        Annotations.synthesized()
      )
    ctx.futureDeclarations.resolve()

    document
  }

  def parseDocument(): Document = parseDocument(Document())

  protected def parseWebApi(map: YMap): WebApi = {

    val api = WebApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node)

    ctx.closedShape(api, map, "webApi")

    map.key("title", (WebApiModel.Name in api).allowingAnnotations)

    map.key("description", (WebApiModel.Description in api).allowingAnnotations)

    map.key(
      "mediaType",
      entry => {
        ctx.globalMediatype = true
        val annotations = Annotations(entry)
        val value: AmfArray = entry.value.tagType match {
          case YType.Seq =>
            ArrayNode(entry.value).text()
          case _ =>
            annotations += SingleValueArray()
            AmfArray(Seq(RamlScalarNode(entry.value).text()), Annotations(entry.value))
        }

        api.setWithoutId(WebApiModel.ContentType, value, annotations)
        api.setWithoutId(WebApiModel.Accepts, value, annotations)
      }
    )

    map.key("version", (WebApiModel.Version in api).allowingAnnotations)
    map.key("termsOfService".asRamlAnnotation, WebApiModel.TermsOfService in api)
    map.key("protocols", (WebApiModel.Schemes in api).allowingSingleValue)
    map.key("contact".asRamlAnnotation, WebApiModel.Provider in api using OrganizationParser.parse)
    map.key("license".asRamlAnnotation, WebApiModel.License in api using LicenseParser.parse)

    map.key(
      "tags".asRamlAnnotation,
      entry => {
        val tags = entry.value.as[Seq[YMap]].map(tag => TagsParser(tag, (tag: Tag) => tag).parse())
        api.setWithoutId(WebApiModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry => ctx.factory.endPointParser(entry, api.withEndPoint, None, endpoints, false).parse())
        api.setWithoutId(WebApiModel.EndPoints, AmfArray(endpoints, Annotations.virtual()), Annotations.inferred())
        ctx.mergeAllOperationContexts()
      }
    )
    RamlServersParser(map, api).parse()
    val idCounter         = new IdCounter()
    val RequirementParser = RamlSecurityRequirementParser.parse(api.id, idCounter) _
    map.key("securedBy", (WebApiModel.Security in api using RequirementParser).allowingSingleValue)
    map.key(
      "documentation",
      entry => {
        api.setWithoutId(
          WebApiModel.Documentations,
          AmfArray(
            UserDocumentationsParser(entry.value.as[Seq[YNode]], ctx.declarations, api.id).parse(),
            Annotations(entry.value)
          ),
          Annotations(entry)
        )
      }
    )

    AnnotationParser(api, map, targetsFor(ctx.contextType))(WebApiShapeParserContextAdapter(ctx)).parse()

    api
  }
}

// todo pass to ctx. declaration parser?
trait Raml10BaseSpecParser extends RamlBaseDocumentParser {

  implicit val ctx: RamlWebApiContext

  override protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "securitySchemes",
      e => {
        addDeclarationKey(DeclarationKey(e))
        e.value.tagType match {
          case YType.Map =>
            e.value.as[YMap].entries.foreach { entry =>
              ctx.declarations += ctx.factory
                .securitySchemeParser(
                  entry,
                  scheme => {
                    val name = entry.key.as[YScalar].text
                    scheme.setWithoutId(
                      SecuritySchemeModel.Name,
                      AmfScalar(name, Annotations(entry.key.value)),
                      Annotations(entry.key)
                    )
                    scheme
                  }
                )
                .parse()
                .add(DeclaredElement())
            }
          case YType.Null =>
          case t =>
            ctx.eh
              .violation(InvalidSecuredByType, parent, s"Invalid type $t for 'securitySchemes' node.", e.value.location)
        }
      }
    )
  }
}

abstract class RamlBaseDocumentParser(implicit ctx: RamlWebApiContext) extends RamlSpecParser with RamlTypeSyntax {
  protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit

  protected def parseDeclarations(root: Root, map: YMap): Unit = {
    val parent = root.location + "#/declarations"
    parseAnnotationTypeDeclarations(map, parent + "/annotations")
    parseTypeDeclarations(map, parent + "/types")
    AbstractDeclarationsParser(
      "traits",
      entry => {
        Trait(entry)
          .withName(entry.key.as[YScalar].text)
          .withId(parent + s"/traits/${entry.key.as[YScalar].text.urlComponentEncoded}")
      },
      map,
      parent + "/traits",
      TraitModel,
      this
    ).parse()
    AbstractDeclarationsParser(
      "resourceTypes",
      entry => {
        ResourceType(entry)
          .withName(entry.key.as[YScalar].text)
          .withId(parent + s"/resourceTypes/${entry.key.as[YScalar].text.urlComponentEncoded}")
      },
      map,
      parent + "/resourceTypes",
      ResourceTypeModel,
      this
    ).parse()
    parseSecuritySchemeDeclarations(map, parent + "/securitySchemes")
    parseParameterDeclarations("parameters".asRamlAnnotation, map, root.location + "#/parameters")
    parseResponsesDeclarations("responses".asRamlAnnotation, map, root.location + "#/responses")
  }

  def parseResponsesDeclarations(key: String, map: YMap, parentPath: String): Unit = {
    map.key(
      key,
      entry => {
        addDeclarationKey(DeclarationKey(entry))
        entry.value
          .as[YMap]
          .entries
          .foreach { e =>
            val node = ScalarNode(e.key)
            val res = OasResponseParser(
              e.value.as[YMap],
              { r: Response =>
                r.withName(node)
                r.annotations ++= Annotations(e)
              }
            )(toOas(ctx))
              .parse()
            res.add(DeclaredElement())
            ctx.declarations += res
          }
      }
    )
  }

  def parseAnnotationTypeDeclarations(map: YMap, customProperties: String): Unit = {
    map.key(
      "annotationTypes",
      e => {
        addDeclarationKey(DeclarationKey(e, isAbstract = true))
        e.value.tagType match {
          case YType.Map =>
            e.value
              .as[YMap]
              .entries
              .map { entry =>
                val typeName = entry.key.as[YScalar].text
                val customProperty = AnnotationTypesParser(
                  entry,
                  customProperty => {
                    customProperty.setWithoutId(
                      CustomDomainPropertyModel.Name,
                      AmfScalar(typeName, Annotations(entry.key.value)),
                      Annotations(entry.key)
                    )
                  }
                )
                ctx.declarations += customProperty.add(DeclaredElement())
              }
          case YType.Null =>
          case t =>
            ctx.eh.violation(
              InvalidAnnotationType,
              customProperties,
              s"Invalid type $t for 'annotationTypes' node.",
              e.value.location
            )
        }
      }
    )
  }

  private def parseTypeDeclarations(map: YMap, parent: String): Unit = {
    typeOrSchema(map).foreach { e =>
      addDeclarationKey(DeclarationKey(e))
      e.value.tagType match {
        case YType.Map =>
          e.value.as[YMap].entries.foreach { entry =>
            val typeName = entry.key.as[YScalar].text
            if (wellKnownType(typeName)) {
              ctx.eh.violation(
                InvalidTypeDefinition,
                parent,
                s"'$typeName' cannot be used to name a custom type",
                entry.key.location
              )
            }
            val parser = Raml10TypeParser(
              entry,
              _ => {}
            )(WebApiShapeParserContextAdapter(ctx))
            parser.parse() match {
              case Some(shape) =>
                shape.setWithoutId(
                  ShapeModel.Name,
                  AmfScalar(typeName, Annotations(entry.key.value)),
                  Annotations(entry.key)
                )
                if (entry.value.tagType == YType.Null) shape.annotations += SynthesizedField()
                ctx.declarations += shape.add(DeclaredElement())
              case None =>
                ctx.eh.violation(UnableToParseShape, parent, s"Error parsing shape '$entry'", entry.location)
            }

          }
        case YType.Null =>
        case t => ctx.eh.violation(InvalidTypesType, parent, s"Invalid type $t for 'types' node.", e.value.location)
      }
    }
  }

  /** Get types or schemas facet. If both are available, default to types facet and throw a validation error. */
  override protected def typeOrSchema(map: YMap): Option[YMapEntry] = {
    val types   = map.key("types")
    val schemas = map.key("schemas")

    for {
      _ <- types
      s <- schemas
    } {
      ctx.eh.violation(
        ExclusiveSchemasType,
        "",
        "'schemas' and 'types' properties are mutually exclusive",
        s.key.location
      )
    }

    schemas.foreach(s =>
      ctx.eh.warning(
        SchemasDeprecated,
        "",
        "'schemas' keyword it's deprecated for 1.0 version, should use 'types' instead",
        s.key.location
      )
    )

    types.orElse(schemas)
  }

  protected def parseParameterDeclarations(key: String, map: YMap, parentPath: String): Unit = {
    map.key(
      key,
      entry => {
        addDeclarationKey(DeclarationKey(entry))
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val typeName      = e.key
            val nameGenerator = new IdCounter()
            val oasParameter: OasParameter = e.value.to[YMap] match {
              case Right(_) =>
                Oas2ParameterParser(YMapEntryLike(e), parentPath, Some(typeName), nameGenerator)(toOas(ctx)).parse()
              case _ =>
                val parameter =
                  Oas2ParameterParser(YMapEntryLike(YMap.empty), parentPath, Some(typeName), nameGenerator)(toOas(ctx))
                    .parse() // todo: links??

                ctx.eh.violation(
                  InvalidParameterType,
                  parameter.domainElement,
                  "Map needed to parse a parameter declaration",
                  e.location
                )
                parameter
            }
            ctx.declarations.registerOasParameter(oasParameter)
          })
      }
    )
  }

}

abstract class RamlSpecParser(implicit ctx: RamlWebApiContext) extends WebApiBaseSpecParser with RamlTypeEntryParser {

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key(
        "usage",
        entry => {
          entry.value.tagType match {
            case YType.Str =>
              val value = ScalarNode(entry.value)
              baseUnit.setWithoutId(BaseUnitModel.Usage, value.string(), Annotations(entry))
            case _ =>
          }
        }
      )
    }
  }

  case class UserDocumentationsParser(seq: Seq[YNode], declarations: WebApiDeclarations, parent: String) {
    def parse(): Seq[CreativeWork] = {
      val results = ListBuffer[CreativeWork]()

      seq.foreach(n =>
        n.tagType match {
          case YType.Map => results += RamlCreativeWorkParser(n)(WebApiShapeParserContextAdapter(ctx)).parse()
          case YType.Seq =>
            ctx.eh.violation(
              InvalidDocumentationType,
              parent,
              s"Unexpected sequence. Options are object or scalar ",
              n.location
            )
          case _ =>
            val scalar = n.as[YScalar]
            declarations.findDocumentations(
              scalar.text,
              SearchScope.Fragments,
              Some((s: String) => ctx.eh.violation(InvalidFragmentType, "", s, scalar.location))
            ) match {
              case Some(doc) =>
                results += doc.link(ScalarNode(n), Annotations.inferred()).asInstanceOf[CreativeWork]
              case _ =>
                ctx.eh.violation(
                  DeclarationNotFound,
                  parent,
                  s"not supported scalar ${scalar.text} for documentation",
                  scalar.location
                )
            }
        }
      )

      results
    }
  }

  object AnnotationTypesParser extends RamlTypeSyntax {
    def apply(ast: YMapEntry, adopt: CustomDomainProperty => Unit): CustomDomainProperty =
      ast.value.tagType match {
        case YType.Map =>
          AnnotationTypesParser(ast, ast.key.as[YScalar].text, ast.value.as[YMap], adopt).parse()

        case YType.Seq =>
          val domainProp = CustomDomainProperty(ast)
          adopt(domainProp)
          ctx.eh.violation(
            InvalidAnnotationType,
            domainProp,
            "Invalid value type for annotation types parser, expected map or scalar reference",
            ast.value.location
          )
          domainProp
        case _ =>
          val key             = ast.key.as[YScalar].text
          val scalar: YScalar = ast.value.as[YScalar]
          val domainProp      = CustomDomainProperty(ast)
          adopt(domainProp)

          ctx.declarations.findAnnotation(scalar.text, SearchScope.All) match {
            case Some(a) =>
              val copied: CustomDomainProperty = a.link(ScalarNode(ast.value), Annotations(ast))
              copied.id = null // we reset the ID so it can be adopted, there's an extra rule where the id is not set
              // because the way they are inserted in the mode later in the parsing
              adopt(copied.withName(key))
              ctx.link(ast.value).left.foreach(_ => copied.add(ExternalFragmentRef(scalar.text)))
              copied
            case _ =>
              Raml10TypeParser(
                YMapEntryLike(ast.value),
                ast.key.as[YScalar].text,
                shape => Unit,
                isAnnotation = true,
                StringDefaultType
              )(WebApiShapeParserContextAdapter(ctx))
                .parse() match {
                case Some(schema) =>
                  tracking(schema, domainProp)
                  domainProp.setWithoutId(CustomDomainPropertyModel.Schema, schema, Annotations.inferred())
                case _ =>
                  ctx.eh.violation(
                    DeclarationNotFound,
                    domainProp,
                    "Could not find declared annotation link in references",
                    scalar.location
                  )
                  domainProp
              }
          }
      }
  }

  case class AnnotationTypesParser(ast: YPart, annotationName: String, map: YMap, adopt: CustomDomainProperty => Unit) {

    def checkValidTarget(entry: YMapEntry, node: AmfObject): Unit = {
      val targets = entry.value.value match {
        case value: YScalar =>
          Seq(value.text)
        case values: YSequence =>
          values.nodes.map(node => node.asScalar.get.text)
      }

      val validTargets: Set[String] = Set(
        "API",
        "DocumentationItem",
        "Resource",
        "Method",
        "Response",
        "RequestBody",
        "ResponseBody",
        "TypeDeclaration",
        "Example",
        "ResourceType",
        "Trait",
        "SecurityScheme",
        "SecuritySchemeSettings",
        "AnnotationType",
        "Library",
        "Overlay",
        "Extension"
      )

      targets.foreach(target => {
        if (!validTargets.contains(target))
          ctx.eh.warning(
            InvalidAllowedTargets,
            node,
            s"$target is not a valid target",
            entry.value.location
          )
      })
    }

    def parse(): CustomDomainProperty = {

      val custom = CustomDomainProperty(ast)
      custom.withName(annotationName)
      adopt(custom)

      // We parse the node as if it were a data shape, this will also check the closed node condition including the
      // annotation type facets
      // Only the value (map or scalar) is the shape. The key is the annotation name, and is not part of the type
      val (maybeAnnotationType, name): (Option[YNode], Option[String]) = ast match {
        case me: YMapEntry => (Some(me.value), me.key.asScalar.map(_.text))
        case n: YNode      => (Some(n), None)
        case m: YMap       => (Some(YNode(m)), None)
        case _             => (None, None)
      }

      maybeAnnotationType match {
        case Some(annotationType) =>
          Raml10TypeParser(
            YMapEntryLike(annotationType),
            name.getOrElse("schema"),
            shape => shape.withName("schema"),
            isAnnotation = true,
            StringDefaultType
          )(WebApiShapeParserContextAdapter(ctx))
            .parse()
            .foreach({ shape =>
              tracking(shape, custom)
              custom.setWithoutId(CustomDomainPropertyModel.Schema, shape, Annotations(annotationType))
            })

          map.key(
            "allowedTargets",
            entry => {
              val annotations = Annotations(entry)
              val targets: AmfArray = entry.value.tagType match {
                case YType.Seq =>
                  checkValidTarget(entry, custom)
                  ArrayNode(entry.value).string()
                case YType.Map =>
                  ctx.eh.violation(
                    InvalidAllowedTargetsType,
                    custom,
                    "Property 'allowedTargets' in a RAML annotation can only be a valid scalar or an array of valid scalars",
                    entry.value.location
                  )
                  AmfArray(Seq())
                case _ =>
                  checkValidTarget(entry, custom)
                  annotations += SingleValueArray()
                  AmfArray(Seq(ScalarNode(entry.value).string()))
              }

              val targetUris = targets.values.map({
                case s: AmfScalar =>
                  VocabularyMappings.ramlToUri.get(s.toString) match {
                    case Some(uri) => AmfScalar(uri, s.annotations)
                    case None      => s
                  }
                case nodeType => AmfScalar(nodeType.toString, nodeType.annotations)
              })

              custom.setWithoutId(
                CustomDomainPropertyModel.Domain,
                AmfArray(targetUris, targets.annotations),
                annotations
              )
            }
          )

          map.key(
            "description",
            entry => {
              val value = ScalarNode(entry.value)
              custom.setWithoutId(CustomDomainPropertyModel.Description, value.string(), Annotations(entry))
            }
          )

          AnnotationParser(custom, map)(WebApiShapeParserContextAdapter(ctx)).parse()

          custom
        case None =>
          ctx.eh.violation(
            InvalidAnnotationType,
            custom,
            "Cannot parse annotation type fragment, cannot find information map",
            ast.location
          )
          custom
      }

    }
  }
}
