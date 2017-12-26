package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.{SingleValueArray, SourceVendor, SynthesizedField}
import amf.core.metamodel.Field
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{AmfArray, AmfElement, AmfScalar}
import amf.core.parser.Annotations
import amf.core.parser._
import amf.core.utils.TemplateUri
import amf.plugins.document.webapi.annotations.DeclaredElement
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.{BaseUriSplitter, WebApiDeclarations}
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.CreativeWork
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Raml 1.0 spec parser
  */
case class Raml10DocumentParser(root: Root)(implicit override val ctx: RamlWebApiContext)
    extends RamlDocumentParser(root)
    with Raml10BaseSpecParser {
  def parseExtension(): Extension = {
    val extension = parseDocument(Extension())

    parseExtension(extension, ExtensionLikeModel.Extends)

    extension
  }

  private def parseExtension(document: Document, field: Field): Unit = {
    val map = root.parsed.document.as[YMap]

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

  def parseOverlay(): Overlay = {
    val overlay = parseDocument(Overlay())

    parseExtension(overlay, ExtensionLikeModel.Extends)

    overlay
  }

  override def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.document.as[YMap]

    val references = ReferencesParser("uses", map, root.references).parse(root.location)
    parseDeclarations(root, map)

    val api = parseWebApi(map).add(SourceVendor(root.vendor))
    document.withEncodes(api)

    val declarables = ctx.declarations.declarables()
    if (declarables.nonEmpty) document.withDeclares(declarables)
    if (references.references.nonEmpty) document.withReferences(references.solvedReferences())

    ctx.futureDeclarations.resolve()

    document
  }
}

abstract class RamlDocumentParser(root: Root)(implicit val ctx: RamlWebApiContext) extends RamlBaseDocumentParser {

  def parseDocument[T <: Document](unit: T): T

  def parseDocument(): Document = parseDocument(Document())

  protected def parseWebApi(map: YMap): WebApi = {

    val api = WebApi(map).adopted(root.location)

    ctx.closedShape(api.id, map, "webApi")

    map.key("title", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Name, value.string(), Annotations(entry))
    })

    map.key(
      "baseUriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          RamlParametersParser(entry.value.as[YMap], api.withBaseUriParameter)
            .parse()
            .map(_.withBinding("path"))
        api.set(WebApiModel.BaseUriParameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "mediaType",
      entry => {
        val annotations = Annotations(entry)
        val value: Option[AmfElement] = entry.value.tagType match {
          case YType.Seq =>
            Some(ArrayNode(entry.value).strings())
          case YType.Map =>
            ctx.violation(api.id, "WebAPI 'mediaType' property must be a scalar or sequence value", entry.value)
            None
          case _ =>
            annotations += SingleValueArray()
            Some(AmfArray(Seq(ValueNode(entry.value).string())))
        }

        value match {
          case Some(mediaType) =>
            api.set(WebApiModel.ContentType, mediaType, annotations)
            api.set(WebApiModel.Accepts, mediaType, annotations)
          case None => // ignore
        }
      }
    )

    map.key("version", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Version, value.text(), Annotations(entry))
    })

    map.key("(termsOfService)", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.TermsOfService, value.string(), Annotations(entry))
    })

    map.key(
      "protocols",
      entry => {
        entry.value.tagType match {
          case YType.Seq =>
            val value = ArrayNode(entry.value)
            api.set(WebApiModel.Schemes, value.strings(), Annotations(entry))
          case YType.Map =>
            ctx.violation(api.id, "WebAPI 'protocols' property must be a scalar or sequence value", entry.value)
          case YType.Str =>
            api.set(WebApiModel.Schemes, AmfArray(Seq(ValueNode(entry.value).string())), Annotations(entry))
          case _ => // Empty protocols node.
        }
      }
    )

    map.key(
      "(contact)",
      entry => {
        val organization: Organization = OrganizationParser(entry.value.as[YMap]).parse()
        api.set(WebApiModel.Provider, organization, Annotations(entry))
      }
    )

    map.key(
      "(license)",
      entry => {
        val license: License = LicenseParser(entry.value.as[YMap]).parse()
        api.set(WebApiModel.License, license, Annotations(entry))
      }
    )

    map.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry => ctx.factory.endPointParser(entry, api.withEndPoint, None, endpoints, false).parse())
        api.set(WebApiModel.EndPoints, AmfArray(endpoints))
      }
    )

    map.key(
      "baseUri",
      entry => {
        val value = entry.value.as[String]
        val uri   = BaseUriSplitter(value)

        if (!TemplateUri.isValid(value))
          ctx.violation(api.id, TemplateUri.invalidMsg(value), entry.value)

        if (api.schemes.isEmpty && uri.protocol.nonEmpty) {
          api.set(WebApiModel.Schemes,
                  AmfArray(Seq(AmfScalar(uri.protocol)), Annotations(entry.value) += SynthesizedField()),
                  Annotations(entry))
        }

        if (uri.domain.nonEmpty) {
          api.set(WebApiModel.Host,
                  AmfScalar(uri.domain, Annotations(entry.value) += SynthesizedField()),
                  Annotations(entry))
        }

        if (uri.path.nonEmpty) {
          api.set(WebApiModel.BasePath,
                  AmfScalar(uri.path, Annotations(entry.value) += SynthesizedField()),
                  Annotations(entry))
        }
      }
    )

    map.key(
      "securedBy",
      entry => {
        val nodes = entry.value.as[Seq[YNode]]
        // TODO check for empty array for resolution ?
        val securedBy =
          nodes
            .map(s => RamlParametrizedSecuritySchemeParser(s, api.withSecurity).parse())

        api.set(WebApiModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "documentation",
      entry => {
        api.setArray(WebApiModel.Documentations,
                     UserDocumentationsParser(entry.value.as[Seq[YNode]], ctx.declarations, api.id).parse(),
                     Annotations(entry))
      }
    )

    AnnotationParser(() => api, map).parse()

    api
  }

}

// todo pass to ctx. declaration parser?
trait Raml10BaseSpecParser extends RamlBaseDocumentParser {

  implicit val ctx: RamlWebApiContext

  override def parseParameterDeclarations(key: String, map: YMap, parentPath: String): Unit = {
    map.key(
      key,
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val parameter =
              Raml10ParameterParser(e, (name) => Parameter().withId(parentPath + "/" + name).withName(name)).parse()
            if (Option(parameter.binding).isEmpty) {
              ctx.violation(parameter.id, "Missing binding information in declared parameter", entry.value)
            }
            ctx.declarations.registerParameter(parameter.add(DeclaredElement()),
                                               Payload().withSchema(parameter.schema))
          })
      }
    )
  }

  override protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "securitySchemes",
      e => {
        e.value.tagType match {
          case YType.Map =>
            e.value.as[YMap].entries.foreach { entry =>
              ctx.declarations += SecuritySchemeParser(entry, scheme => scheme.withName(entry.key).adopted(parent))
                .parse()
                .add(DeclaredElement())
            }
          case YType.Null =>
          case t          => ctx.violation(parent, s"Invalid type $t for 'securitySchemes' node.", e.value)
        }
      }
    )
  }
}

abstract class RamlBaseDocumentParser(implicit ctx: RamlWebApiContext) extends RamlSpecParser {
  protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit

  protected def parseDeclarations(root: Root, map: YMap): Unit = {
    val parent = root.location + "#/declarations"
    parseTypeDeclarations(map, parent)
    parseAnnotationTypeDeclarations(map, parent)
    AbstractDeclarationsParser("resourceTypes", entry => ResourceType(entry), map, parent)
      .parse()
    AbstractDeclarationsParser("traits", entry => Trait(entry), map, parent).parse()
    parseSecuritySchemeDeclarations(map, parent)
    parseParameterDeclarations("(parameters)", map, root.location + "#/parameters")
  }

  def parseAnnotationTypeDeclarations(map: YMap, customProperties: String): Unit = {
    map.key(
      "annotationTypes",
      e => {
        e.value.tagType match {
          case YType.Map =>
            e.value
              .as[YMap]
              .entries
              .map { entry =>
                val typeName = entry.key.as[String]
                val customProperty = AnnotationTypesParser(entry,
                                                           customProperty =>
                                                             customProperty
                                                               .withName(typeName)
                                                               .adopted(customProperties))
                ctx.declarations += customProperty.add(DeclaredElement())
              }
          case YType.Null =>
          case t          => ctx.violation(customProperties, s"Invalid type $t for 'annotationTypes' node.", e.value)
        }
      }
    )
  }

  private def parseTypeDeclarations(map: YMap, parent: String): Unit = {
    typeOrSchema(map).foreach { e =>
      e.value.tagType match {
        case YType.Map =>
          e.value.as[YMap].entries.foreach { entry =>
            Raml10TypeParser(entry, shape => shape.withName(entry.key).adopted(parent))
              .parse() match {
              case Some(shape) =>
                if (entry.value.tagType == YType.Null) shape.annotations += SynthesizedField()
                ctx.declarations += shape.add(DeclaredElement())
              case None => ctx.violation(parent, s"Error parsing shape '$entry'", entry)
            }

          }
        case YType.Null =>
        case t          => ctx.violation(parent, s"Invalid type $t for 'types' node.", e.value)
      }
    }
  }

  /** Get types or schemas facet. If both are available, default to types facet and throw a validation error. */
  private def typeOrSchema(map: YMap) = {
    val types   = map.key("types")
    val schemas = map.key("schemas")

    for {
      _ <- types
      s <- schemas
    } {
      ctx.violation("'schemas' and 'types' properties are mutually exclusive", Some(s.key))
    }

    types.orElse(schemas)
  }

  def parseParameterDeclarations(key: String, map: YMap, parentPath: String): Unit

}

abstract class RamlSpecParser(implicit ctx: RamlWebApiContext) extends BaseSpecParser {

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key("usage", entry => {
        val value = ValueNode(entry.value)
        baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
      })
    }
  }

  case class UserDocumentationsParser(seq: Seq[YNode], declarations: WebApiDeclarations, parent: String) {
    def parse(): Seq[CreativeWork] = {
      val results = ListBuffer[CreativeWork]()

      seq.foreach(n =>
        n.tagType match {
          case YType.Map => results += RamlCreativeWorkParser(n.as[YMap], withExtention = true).parse()
          case YType.Seq =>
          case _ =>
            val scalar = n.as[YScalar]
            declarations.findDocumentations(scalar.text, SearchScope.Fragments) match {
              case Some(doc) =>
                results += doc.link(scalar.text, Annotations()).asInstanceOf[CreativeWork]
              case _ =>
                ctx.violation(parent, s"not supported scalar ${scalar.text} for documentation", scalar)
            }
      })

      results
    }
  }

  object AnnotationTypesParser extends RamlTypeSyntax {
    def apply(ast: YMapEntry, adopt: (CustomDomainProperty) => Unit): CustomDomainProperty =
      ast.value.tagType match {
        case YType.Map =>
          AnnotationTypesParser(ast, ast.key.as[YScalar].text, ast.value.as[YMap], adopt).parse()

        case YType.Seq =>
          val domainProp = CustomDomainProperty()
          adopt(domainProp)
          ctx.violation(domainProp.id,
                        "Invalid value type for annotation types parser, expected map or scalar reference",
                        ast.value)
          domainProp
        case _ =>
          val key             = ast.key.as[YScalar].text
          val scalar: YScalar = ast.value.as[YScalar]
          val domainProp      = CustomDomainProperty()
          adopt(domainProp)

          ctx.declarations.findAnnotation(scalar.text, SearchScope.All) match {
            case Some(a) =>
              val copied: CustomDomainProperty = a.link(scalar.text, Annotations(ast))
              copied.id = null // we reset the ID so ti can be adopted, there's an extra rule where the id is not set
              // because the way they are inserted in the mode later in the parsing
              adopt(copied.withName(key))
              copied
            case _ =>
              Raml10TypeParser(ast, (shape) => shape.adopted(domainProp.id), isAnnotation = true).parse() match {
                case Some(schema) => domainProp.withSchema(schema)
                case _ =>
                  ctx.violation(domainProp.id, "Could not find declared annotation link in references", scalar)
                  domainProp
              }
          }
      }
  }

  case class AnnotationTypesParser(ast: YPart,
                                   annotationName: String,
                                   map: YMap,
                                   adopt: (CustomDomainProperty) => Unit) {
    def parse(): CustomDomainProperty = {

      val custom = CustomDomainProperty(ast)
      custom.withName(annotationName)
      adopt(custom)

      // We parse the node as if it were a data shape, this will also check the closed node condition including the
      // annotation type facets
      val maybeAnnotationType: Option[YMapEntry] = ast match {
        case me: YMapEntry => Some(me)
        case m: YMap       => Some(YMapEntry(YNode("annotationType"), YNode(m)))
        case _             => None
      }

      maybeAnnotationType match {
        case Some(annotationType) =>
          Raml10TypeParser(annotationType, shape => shape.adopted(custom.id), isAnnotation = true)
            .parse()
            .foreach({ shape =>
              custom.set(CustomDomainPropertyModel.Schema, shape, Annotations(ast))
            })

          map.key(
            "allowedTargets",
            entry => {
              val annotations = Annotations(entry)
              val targets: AmfArray = entry.value.tagType match {
                case YType.Seq =>
                  ArrayNode(entry.value).strings()
                case YType.Map =>
                  ctx.violation(
                    custom.id,
                    "Property 'allowedTargets' in a RAML annotation can only be a valid scalar or an array of valid scalars",
                    entry.value)
                  AmfArray(Seq())
                case _ =>
                  annotations += SingleValueArray()
                  AmfArray(Seq(ValueNode(entry.value).string()))
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
            val value = ValueNode(entry.value)
            custom.set(CustomDomainPropertyModel.Description, value.string(), Annotations(entry))
          })

          AnnotationParser(() => custom, map).parse()

          custom
        case None =>
          ctx.violation(custom.id, "Cannot parse annotation type fragment, cannot find information map", ast)
          custom
      }

    }
  }
}
