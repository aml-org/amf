package amf.spec.raml

import amf.compiler.Root
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation._
import amf.domain._
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.metadata.document.BaseUnitModel
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.parser.{YMapOps, YScalarYRead}
import amf.spec.common._
import amf.spec.declaration._
import amf.spec.domain._
import amf.spec.{BaseUriSplitter, Declarations, ParserContext}
import amf.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Raml 1.0 spec parser
  */
case class RamlDocumentParser(root: Root)(implicit val ctx: ParserContext) extends RamlSpecParser {

  def parseDocument(): Document = {

    val document = Document().adopted(root.location)

    val map = root.document.as[YMap]

    val references = ReferencesParser("uses", map, root.references).parse(root.location)
    parseDeclarations(root, map, references.declarations)

    val api = parseWebApi(map, references.declarations).add(SourceVendor(root.vendor))
    document.withEncodes(api)

    val declarables = references.declarations.declarables()
    if (declarables.nonEmpty) document.withDeclares(declarables)
    if (references.references.nonEmpty) document.withReferences(references.solvedReferences())

    document
  }

  def parseWebApi(map: YMap, declarations: Declarations): WebApi = {

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
          RamlParametersParser(entry.value.as[YMap], api.withBaseUriParameter, declarations)
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
          case _ =>
            api.set(WebApiModel.Schemes, AmfArray(Seq(ValueNode(entry.value).string())), Annotations(entry))
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
        entries.foreach(entry => RamlEndpointParser(entry, api.withEndPoint, None, endpoints, declarations).parse())
        api.set(WebApiModel.EndPoints, AmfArray(endpoints))
      }
    )

    map.key(
      "baseUri",
      entry => {
        val value = ValueNode(entry.value)
        val uri   = BaseUriSplitter(value.string().value.toString)

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
            .map(s => RamlParametrizedSecuritySchemeParser(s, api.withSecurity, declarations).parse())

        api.set(WebApiModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "documentation",
      entry => {
        api.setArray(WebApiModel.Documentations,
                     UserDocumentationsParser(entry.value.as[Seq[YNode]], declarations, api.id).parse(),
                     Annotations(entry))
      }
    )

    AnnotationParser(() => api, map).parse()

    api
  }

}

abstract class RamlSpecParser extends BaseSpecParser {

  protected def parseDeclarations(root: Root, map: YMap, declarations: Declarations): Unit = {
    val parent = root.location + "#/declarations"
    parseTypeDeclarations(map, parent, declarations)
    parseAnnotationTypeDeclarations(map, parent, declarations)
    AbstractDeclarationsParser("resourceTypes", (entry: YMapEntry) => ResourceType(entry), map, parent, declarations)
      .parse()
    AbstractDeclarationsParser("traits", (entry: YMapEntry) => Trait(entry), map, parent, declarations).parse()
    parseSecuritySchemeDeclarations(map, parent, declarations)
    parseParameterDeclarations("(parameters)", map, root.location + "#/parameters", declarations)
    declarations.resolve()
  }

  def parseAnnotationTypeDeclarations(map: YMap, customProperties: String, declarations: Declarations): Unit = {

    map.key(
      "annotationTypes",
      e => {
        e.value
          .as[YMap]
          .entries
          .map(entry => {
            val typeName = entry.key.as[String]
            val customProperty = AnnotationTypesParser(entry,
                                                       customProperty =>
                                                         customProperty
                                                           .withName(typeName)
                                                           .adopted(customProperties),
                                                       declarations)
            declarations += customProperty.add(DeclaredElement())
          })
      }
    )
  }

  private def parseTypeDeclarations(map: YMap, parent: String, declarations: Declarations): Unit = {
    map.key(
      "types",
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          RamlTypeParser(entry, shape => shape.withName(entry.key).adopted(parent), declarations)
            .parse() match {
            case Some(shape) => declarations += shape.add(DeclaredElement())
            case None        => ctx.violation(parent, s"Error parsing shape '$entry'", entry)
          }
        }
      }
    )
  }

  private def parseSecuritySchemeDeclarations(map: YMap, parent: String, declarations: Declarations): Unit = {
    map.key(
      "securitySchemes",
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          declarations += SecuritySchemeParser(entry,
                                               scheme => scheme.withName(entry.key).adopted(parent),
                                               declarations).parse().add(DeclaredElement())
        }
      }
    )
  }

  def parseParameterDeclarations(key: String, map: YMap, parentPath: String, declarations: Declarations): Unit = {
    map.key(
      key,
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val parameter = RamlParameterParser(e,
                                                (name) => Parameter().withId(parentPath + "/" + name).withName(name),
                                                declarations).parse()
            if (Option(parameter.binding).isEmpty) {
              ctx.violation(parameter.id, "Missing binding information in declared parameter", entry.value)
            }
            declarations.registerParameter(parameter.add(DeclaredElement()), Payload().withSchema(parameter.schema))
          })
      }
    )
  }

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key("usage", entry => {
        val value = ValueNode(entry.value)
        baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
      })
    }
  }

  case class UserDocumentationsParser(seq: Seq[YNode], declarations: Declarations, parent: String) {
    def parse(): Seq[CreativeWork] = {
      val results = ListBuffer[CreativeWork]()

      seq.foreach(n =>
        n.tagType match {
          case YType.Map => results += RamlCreativeWorkParser(n.as[YMap], withExtention = true).parse()
          case YType.Seq =>
          case _ =>
            val scalar = n.as[YScalar]
            declarations.findDocumentations(scalar.text) match {
              case Some(doc) =>
                results += doc.link(scalar.text, Annotations()).asInstanceOf[CreativeWork]
              case _ =>
                ctx.violation(parent, s"not supported scalar ${scalar.text} for documentation", scalar)
            }
      })

      results
    }
  }

  object AnnotationTypesParser {
    def apply(ast: YMapEntry,
              adopt: (CustomDomainProperty) => Unit,
              declarations: Declarations): CustomDomainProperty =
      ast.value.tagType match {
        case YType.Map =>
          AnnotationTypesParser(ast, ast.key.as[YScalar].text, ast.value.as[YMap], adopt, declarations).parse()

        case YType.Seq =>
          val domainProp = CustomDomainProperty()
          adopt(domainProp)
          ctx.violation(domainProp.id,
                        "Invalid value type for annotation types parser, expected map or scalar reference",
                        ast.value)
          domainProp
        case _ =>
          // @todo: this can be a scalar with a type expression, not a reference

          LinkedAnnotationTypeParser(ast, ast.key.as[YScalar].text, ast.value.as[YScalar], adopt, declarations).parse()
      }
  }

  case class LinkedAnnotationTypeParser(ast: YPart,
                                        annotationName: String,
                                        scalar: YScalar,
                                        adopt: (CustomDomainProperty) => Unit,
                                        declarations: Declarations) {
    def parse(): CustomDomainProperty = {
      declarations
        .findAnnotation(scalar.text)
        .map { a =>
          val copied: CustomDomainProperty = a.link(scalar.text, Annotations(ast))
          adopt(copied.withName(annotationName))
          copied
        }
        .getOrElse {
          val domainProperty = CustomDomainProperty()
          adopt(domainProperty)
          ctx.violation(domainProperty.id, "Could not find declared annotation link in references", scalar)
          domainProperty
        }
    }
  }

  case class AnnotationTypesParser(ast: YPart,
                                   annotationName: String,
                                   map: YMap,
                                   adopt: (CustomDomainProperty) => Unit,
                                   declarations: Declarations) {
    def parse(): CustomDomainProperty = {

      val custom = CustomDomainProperty(ast)
      custom.withName(annotationName)
      adopt(custom)

      // We parse the node as if it were a data shape, this will also check the closed node condition including the
      // annotation type facets
      RamlTypeParser(ast.asInstanceOf[YMapEntry], shape => shape.adopted(custom.id), declarations, isAnnotation = true)
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
    }
  }
}
