package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations._
import amf.core.metamodel.Field
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.model.document._
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils._
import amf.plugins.document.webapi.annotations.DeclarationKey
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContextType.RamlWebApiContextType
import amf.plugins.document.webapi.contexts.parser.raml.{ExtensionLikeWebApiContext, RamlWebApiContext, RamlWebApiContextType}
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.raml.RamlAnnotationTargets.targetsFor
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.ResponseModel
import amf.plugins.domain.webapi.metamodel.api.WebApiModel
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.metamodel.templates.{ResourceTypeModel, TraitModel}
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.api.WebApi
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.features.validation.CoreValidations.DeclarationNotFound
import amf.validations.ParserSideValidations._
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Extension and Overlay parser
  */
case class ExtensionLikeParser(root: Root)(implicit override val ctx: ExtensionLikeWebApiContext)
    extends RamlDocumentParser(root)
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

  private def collectAncestorsDeclarationsAndReferences(reference: BaseUnit, collector: RamlWebApiDeclarations): Unit = {

    reference.asInstanceOf[Document].declares.foreach(collector += _)

    val (exLikeOrDocument, otherReferences) = reference.references.partition({
      case _: ExtensionLike[_] | _: Document => true
      case _                                 => false
    })

    reference.annotations
      .find(classOf[Aliases])
      .foreach(_.aliases.foreach { alias =>
        val fullUrl = alias._2._1
        otherReferences.find(_.location().exists(_.equals(fullUrl))) match {
          case Some(library: Module) =>
            val libraryDeclarations = collector.getOrCreateLibrary(alias._1)
            library.declares.foreach(libraryDeclarations += _)
          case _ => // nothing to do
        }
      })

    exLikeOrDocument.foreach(collectAncestorsDeclarationsAndReferences(_, collector))
  }

  private def parseExtension(document: Document, field: Field): Unit = {
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

    UsageParser(map, document).parse()

    map
      .key("extends")
      .foreach(e => {
        root.references
          .find(_.origin.url == e.value.as[String])
          .foreach(extend =>
            document
              .set(field, AmfScalar(extend.unit.id, Annotations(e.value)), Annotations(e)))
      })
  }
}

object ExtensionLikeParser {
  def apply(root: Root, baseCtx: RamlWebApiContext): ExtensionLikeParser = {
    val parentDeclarations = new RamlWebApiDeclarations(alias = None,
                                                        errorHandler = baseCtx.eh,
                                                        futureDeclarations = baseCtx.futureDeclarations)
    val exLikeCtx: ExtensionLikeWebApiContext = new ExtensionLikeWebApiContext(baseCtx.rootContextDocument,
                                                                               baseCtx.refs,
                                                                               baseCtx,
                                                                               Some(baseCtx.declarations),
                                                                               parentDeclarations,
                                                                               options = baseCtx.options)
    new ExtensionLikeParser(root)(exLikeCtx)
  }
}

/**
  * Raml 1.0 spec parser
  */
case class Raml10DocumentParser(root: Root)(implicit override val ctx: RamlWebApiContext)
    extends RamlDocumentParser(root)
    with Raml10BaseSpecParser {}

abstract class RamlDocumentParser(root: Root)(implicit val ctx: RamlWebApiContext) extends RamlBaseDocumentParser {

  def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]

    val references = ReferencesParser(document, root.location, "uses", map, root.references).parse()
    parseDeclarations(root, map)
    val api = parseWebApi(map).add(SourceVendor(ctx.vendor))

    document.withEncodes(api)

    addDeclarationsToModel(document)
    if (references.nonEmpty) document.withReferences(references.baseUnitReferences())

    ctx.futureDeclarations.resolve()

    document
  }

  def parseDocument(): Document = parseDocument(Document())

  protected def parseWebApi(map: YMap): WebApi = {

    val api = WebApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node).adopted(root.location)

    ctx.closedShape(api.id, map, "webApi")

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

        api.set(WebApiModel.ContentType, value, annotations)
        api.set(WebApiModel.Accepts, value, annotations)
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
        val tags = entry.value.as[Seq[YMap]].map(tag => TagsParser(tag, (tag: Tag) => tag.adopted(api.id)).parse())
        api.set(WebApiModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry => ctx.factory.endPointParser(entry, api.withEndPoint, None, endpoints, false).parse())
        api.set(WebApiModel.EndPoints, AmfArray(endpoints))
        ctx.mergeAllOperationContexts()
      }
    )
    RamlServersParser(map, api).parse()
    val idCounter         = new IdCounter()
    val RequirementParser = RamlSecurityRequirementParser.parse(api.withSecurity, idCounter) _
    map.key("securedBy", (WebApiModel.Security in api using RequirementParser).allowingSingleValue)
    map.key(
      "documentation",
      entry => {
        api.set(WebApiModel.Documentations,
                AmfArray(UserDocumentationsParser(entry.value.as[Seq[YNode]], ctx.declarations, api.id).parse()),
                Annotations(entry))
      }
    )

    AnnotationParser(api, map, targetsFor(ctx.contextType)).parse()

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
                    scheme.set(SecuritySchemeModel.Name,
                               AmfScalar(name, Annotations(entry.key.value)),
                               Annotations(entry.key))
                    scheme.adopted(parent)
                  }
                )
                .parse()
                .add(DeclaredElement())
            }
          case YType.Null =>
          case t =>
            ctx.eh.violation(InvalidSecuredByType, parent, s"Invalid type $t for 'securitySchemes' node.", e.value)
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
            val node = ScalarNode(e.key).text()
            val res = OasResponseParser(e.value.as[YMap], { r: Response =>
              r.set(ResponseModel.Name, node).adopted(parentPath)
              r.annotations ++= Annotations(e)
            })(toOas(ctx))
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
                    customProperty.set(CustomDomainPropertyModel.Name,
                                       AmfScalar(typeName, Annotations(entry.key.value)),
                                       Annotations(entry.key))
                    customProperty.adopted(customProperties)
                  }
                )
                ctx.declarations += customProperty.add(DeclaredElement())
              }
          case YType.Null =>
          case t =>
            ctx.eh.violation(InvalidAnnotationType,
                             customProperties,
                             s"Invalid type $t for 'annotationTypes' node.",
                             e.value)
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
                entry.key
              )
            }
            val parser = Raml10TypeParser(
              entry,
              shape => {
                shape.set(ShapeModel.Name, AmfScalar(typeName, Annotations(entry.key.value)), Annotations(entry.key))
                shape.adopted(parent)
              }
            )
            parser.parse() match {
              case Some(shape) =>
                if (entry.value.tagType == YType.Null) shape.annotations += SynthesizedField()
                ctx.declarations += shape.add(DeclaredElement())
              case None => ctx.eh.violation(UnableToParseShape, parent, s"Error parsing shape '$entry'", entry)
            }

          }
        case YType.Null =>
        case t          => ctx.eh.violation(InvalidTypesType, parent, s"Invalid type $t for 'types' node.", e.value)
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
      ctx.eh.violation(ExclusiveSchemasType, "", "'schemas' and 'types' properties are mutually exclusive", s.key)
    }

    schemas.foreach(
      s =>
        ctx.eh.warning(SchemasDeprecated,
                       "",
                       "'schemas' keyword it's deprecated for 1.0 version, should use 'types' instead",
                       s.key))

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
            val oasParameter: domain.OasParameter = e.value.to[YMap] match {
              case Right(_) =>
                Oas2ParameterParser(YMapEntryLike(e), parentPath, Some(typeName), nameGenerator)(toOas(ctx)).parse()
              case _ =>
                val parameter =
                  Oas2ParameterParser(YMapEntryLike(YMap.empty), parentPath, Some(typeName), nameGenerator)(toOas(ctx))
                    .parse() // todo: links??

                ctx.eh.violation(InvalidParameterType,
                                 parameter.domainElement.id,
                                 "Map needed to parse a parameter declaration",
                                 e)
                parameter
            }
            ctx.declarations.registerOasParameter(oasParameter)
          })
      }
    )
  }

}

abstract class RamlSpecParser(implicit ctx: RamlWebApiContext) extends WebApiBaseSpecParser {

  protected def typeOrSchema(map: YMap): Option[YMapEntry] = map.key("type").orElse(map.key("schema"))

  protected def nestedTypeOrSchema(map: YMap): Option[YMapEntry] = map.key("type").orElse(map.key("schema")) match {
    case Some(n) if n.value.tagType == YType.Map =>
      nestedTypeOrSchema(n.value.as[YMap])
    case res =>
      res
  }

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key(
        "usage",
        entry => {
          entry.value.tagType match {
            case YType.Str =>
              val value = ScalarNode(entry.value)
              baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
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
          case YType.Map => results += RamlCreativeWorkParser(n).parse()
          case YType.Seq =>
            ctx.eh.violation(InvalidDocumentationType,
                             parent,
                             s"Unexpected sequence. Options are object or scalar ",
                             n)
          case _ =>
            val scalar = n.as[YScalar]
            declarations.findDocumentations(
              scalar.text,
              SearchScope.Fragments,
              Some((s: String) => ctx.eh.violation(InvalidFragmentType, "", s, scalar))) match {
              case Some(doc) =>
                results += doc.link(scalar.text, Annotations()).asInstanceOf[CreativeWork]
              case _ =>
                ctx.eh.violation(DeclarationNotFound,
                                 parent,
                                 s"not supported scalar ${scalar.text} for documentation",
                                 scalar)
            }
      })

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
          ctx.eh.violation(InvalidAnnotationType,
                           domainProp.id,
                           "Invalid value type for annotation types parser, expected map or scalar reference",
                           ast.value)
          domainProp
        case _ =>
          val key             = ast.key.as[YScalar].text
          val scalar: YScalar = ast.value.as[YScalar]
          val domainProp      = CustomDomainProperty(ast)
          adopt(domainProp)

          ctx.declarations.findAnnotation(scalar.text, SearchScope.All) match {
            case Some(a) =>
              val copied: CustomDomainProperty = a.link(scalar.text, Annotations(ast))
              copied.id = null // we reset the ID so it can be adopted, there's an extra rule where the id is not set
              // because the way they are inserted in the mode later in the parsing
              adopt(copied.withName(key))
              ctx.link(ast.value).left.foreach(_ => copied.add(ExternalFragmentRef(scalar.text)))
              copied
            case _ =>
              Raml10TypeParser(YMapEntryLike(ast.value),
                               ast.key.as[YScalar].text,
                               shape => shape.adopted(domainProp.id),
                               isAnnotation = true,
                               StringDefaultType)
                .parse() match {
                case Some(schema) =>
                  tracking(schema, domainProp.id)
                  domainProp.withSchema(schema)
                case _ =>
                  ctx.eh.violation(DeclarationNotFound,
                                   domainProp.id,
                                   "Could not find declared annotation link in references",
                                   scalar)
                  domainProp
              }
          }
      }
  }

  case class AnnotationTypesParser(ast: YPart, annotationName: String, map: YMap, adopt: CustomDomainProperty => Unit) {

    def checkValidTarget(entry: YMapEntry, nodeId: String): Unit = {
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
            nodeId,
            s"$target is not a valid target",
            entry.value
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
          Raml10TypeParser(YMapEntryLike(annotationType),
                           name.getOrElse("schema"),
                           shape => shape.withName("schema").adopted(custom.id),
                           isAnnotation = true,
                           StringDefaultType)
            .parse()
            .foreach({ shape =>
              tracking(shape, custom.id)
              custom.set(CustomDomainPropertyModel.Schema,
                         shape,
                         maybeAnnotationType.map(Annotations(_)).getOrElse(Annotations()))
            })

          map.key(
            "allowedTargets",
            entry => {
              val annotations = Annotations(entry)
              val targets: AmfArray = entry.value.tagType match {
                case YType.Seq =>
                  checkValidTarget(entry, custom.id)
                  ArrayNode(entry.value).string()
                case YType.Map =>
                  ctx.eh.violation(
                    InvalidAllowedTargetsType,
                    custom.id,
                    "Property 'allowedTargets' in a RAML annotation can only be a valid scalar or an array of valid scalars",
                    entry.value
                  )
                  AmfArray(Seq())
                case _ =>
                  checkValidTarget(entry, custom.id)
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

              custom.set(CustomDomainPropertyModel.Domain, AmfArray(targetUris), annotations)
            }
          )

          map.key("description", entry => {
            val value = ScalarNode(entry.value)
            custom.set(CustomDomainPropertyModel.Description, value.string(), Annotations(entry))
          })

          AnnotationParser(custom, map).parse()

          custom
        case None =>
          ctx.eh.violation(InvalidAnnotationType,
                           custom.id,
                           "Cannot parse annotation type fragment, cannot find information map",
                           ast)
          custom
      }

    }
  }
}
