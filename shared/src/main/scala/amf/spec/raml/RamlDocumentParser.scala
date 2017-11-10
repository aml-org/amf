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
import amf.parser.{YMapOps, YValueOps}
import amf.remote.{Raml, Vendor}
import amf.spec.common._
import amf.spec.declaration._
import amf.spec.domain._
import amf.spec.{BaseUriSplitter, Declarations}
import amf.validation.Validation
import amf.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Raml 1.0 spec parser
  */
case class RamlDocumentParser(root: Root, currentValidation: Validation)
    extends RamlSpecParser(currentValidation)
    with RamlSyntax {

  def parseDocument(): Document = {

    val document = Document().adopted(root.location)

    root.document.value.foreach(value => {
      val map = value.toMap

      val references = ReferencesParser("uses", map, root.references).parse()
      parseDeclarations(root, map, references.declarations)

      val api = parseWebApi(map, references.declarations).add(SourceVendor(root.vendor))
      document.withEncodes(api)

      val declarables = references.declarations.declarables()
      if (declarables.nonEmpty) document.withDeclares(declarables)
      if (references.references.nonEmpty) document.withReferences(references.solvedReferences())
    })
    document
  }

  def parseWebApi(map: YMap, declarations: Declarations): WebApi = {

    val api = WebApi(map).adopted(root.location)

    validateClosedShape(currentValidation, api.id, map, "webApi")

    map.key("title", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Name, value.string(), Annotations(entry))
    })

    map.key(
      "baseUriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          RamlParametersParser(entry.value.value.toMap, api.withBaseUriParameter, declarations, currentValidation)
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
        val value: Option[AmfElement] = entry.value.value match {
          case _: YScalar =>
            annotations += SingleValueArray()
            Some(AmfArray(Seq(ValueNode(entry.value).string())))
          case _: YSequence =>
            Some(ArrayNode(entry.value.value.toSequence).strings())
          case _ =>
            parsingErrorReport(currentValidation,
                               api.id,
                               "WebAPI 'mediaType' property must be a scalar or sequence value",
                               Some(entry.value.value))
            None
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
      api.set(WebApiModel.Version, value.string(), Annotations(entry))
    })

    map.key("(termsOfService)", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.TermsOfService, value.string(), Annotations(entry))
    })

    map.key(
      "protocols",
      entry => {
        entry.value.value match {
          case _: YScalar =>
            api.set(WebApiModel.Schemes, AmfArray(Seq(ValueNode(entry.value).string())), Annotations(entry))
          case _: YSequence =>
            val value = ArrayNode(entry.value.value.toSequence)
            api.set(WebApiModel.Schemes, value.strings(), Annotations(entry))
          case _ =>
            parsingErrorReport(currentValidation,
                               api.id,
                               "WebAPI 'protocols' property must be a scalar or sequence value",
                               Some(entry.value.value))
        }
      }
    )

    map.key(
      "(contact)",
      entry => {
        val organization: Organization = OrganizationParser(entry.value.value.toMap, currentValidation).parse()
        api.set(WebApiModel.Provider, organization, Annotations(entry))
      }
    )

    map.key(
      "(license)",
      entry => {
        val license: License = LicenseParser(entry.value.value.toMap, currentValidation).parse()
        api.set(WebApiModel.License, license, Annotations(entry))
      }
    )

    map.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry =>
          RamlEndpointParser(entry, api.withEndPoint, None, endpoints, declarations, currentValidation).parse())
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
        // TODO check for empty array for resolution ?
        val securedBy =
          entry.value.value.toSequence.nodes
            .collect({ case v: YNode => v })
            .map(s => RamlParametrizedSecuritySchemeParser(s, api.withSecurity, declarations).parse())

        api.set(WebApiModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "documentation",
      entry => {
        api.setArray(WebApiModel.Documentations,
                     UserDocumentationsParser(entry.value.value.toSequence, declarations, api.id).parse(),
                     Annotations(entry))
      }
    )

    AnnotationParser(() => api, map).parse()

    api
  }

}

abstract class RamlSpecParser(currentValidation: Validation) extends BaseSpecParser {

  override implicit val spec: SpecParserContext = RamlSpecParserContext

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
        e.value.value.toMap.entries.map(entry => {
          val typeName = entry.key.value.toScalar.text
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
        e.value.value.toMap.entries.foreach { entry =>
          RamlTypeParser(entry, shape => shape.withName(entry.key).adopted(parent), declarations, currentValidation)
            .parse() match {
            case Some(shape) => declarations += shape.add(DeclaredElement())
            case None =>
              parsingErrorReport(currentValidation, parent, s"Error parsing shape '$entry'", Some(e.value.value))
          }
        }
      }
    )
  }

  private def parseSecuritySchemeDeclarations(map: YMap, parent: String, declarations: Declarations): Unit = {
    map.key(
      "securitySchemes",
      e => {
        e.value.value.toMap.entries.foreach { entry =>
          declarations += SecuritySchemeParser(entry,
                                               scheme => scheme.withName(entry.key).adopted(parent),
                                               declarations,
                                               currentValidation).parse().add(DeclaredElement())
        }
      }
    )
  }

  def parseParameterDeclarations(key: String, map: YMap, parentPath: String, declarations: Declarations): Unit = {
    map.key(
      key,
      entry => {
        entry.value.value.toMap.entries.foreach(e => {
          val parameter = RamlParameterParser(e,
                                              (name) => Parameter().withId(parentPath + "/" + name).withName(name),
                                              declarations,
                                              currentValidation).parse()
          if (Option(parameter.binding).isEmpty) {
            parsingErrorReport(currentValidation,
                               parameter.id,
                               "Missing binding information in declared parameter",
                               Some(entry.value.value))
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

  case class UserDocumentationsParser(seq: YSequence, declarations: Declarations, parent: String) {
    def parse(): Seq[CreativeWork] = {
      val results = ListBuffer[CreativeWork]()

      seq.nodes
        .foreach(n =>
          n.value match {
            case m: YMap => results += RamlCreativeWorkParser(m, withExtention = true).parse()
            case scalar: YScalar =>
              declarations.findDocumentations(scalar.text) match {
                case Some(doc) =>
                  results += doc.link(scalar.text, Annotations()).asInstanceOf[CreativeWork]
                case _ =>
                  parsingErrorReport(currentValidation,
                                     parent,
                                     s"not supported scalar ${scalar.text} for documentation",
                                     Some(n.value))
              }
        })

      results
    }
  }

  object AnnotationTypesParser {
    def apply(ast: YMapEntry,
              adopt: (CustomDomainProperty) => Unit,
              declarations: Declarations): CustomDomainProperty =
      ast.value.value match {
        case map: YMap => AnnotationTypesParser(ast, ast.key.value.toScalar.text, map, adopt, declarations).parse()
        // @todo: this can be a scalar with a type expression, not a reference
        case scalar: YScalar =>
          LinkedAnnotationTypeParser(ast, ast.key.value.toScalar.text, scalar, adopt, declarations).parse()
        case _ =>
          val domainProp = CustomDomainProperty()
          adopt(domainProp)
          parsingErrorReport(currentValidation,
                             domainProp.id,
                             "Invalid value type for annotation types parser, expected map or scalar reference",
                             Some(ast.value.value))
          domainProp
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
          parsingErrorReport(currentValidation,
                             domainProperty.id,
                             "Could not find declared annotation link in references",
                             Some(ast))
          domainProperty
        }
    }
  }

  case class AnnotationTypesParser(ast: YPart,
                                   annotationName: String,
                                   map: YMap,
                                   adopt: (CustomDomainProperty) => Unit,
                                   declarations: Declarations)
      extends RamlSyntax {
    def parse(): CustomDomainProperty = {

      val custom = CustomDomainProperty(ast)
      custom.withName(annotationName)
      adopt(custom)

      // We parse the node as if it were a data shape, this will also check the closed node condition including the
      // annotation type facets
      RamlTypeParser(ast.asInstanceOf[YMapEntry], shape => shape.adopted(custom.id), declarations, currentValidation)
        .parse()
        .foreach({ shape =>
          custom.set(CustomDomainPropertyModel.Schema, shape, Annotations(ast))
        })

      map.key(
        "allowedTargets",
        entry => {
          val annotations = Annotations(entry)
          val targets: AmfArray = entry.value.value match {
            case _: YScalar =>
              annotations += SingleValueArray()
              AmfArray(Seq(ValueNode(entry.value).string()))
            case sequence: YSequence =>
              ArrayNode(sequence).strings()
            case _ =>
              parsingErrorReport(
                currentValidation,
                custom.id,
                "Property 'allowedTargets' in a RAML annotation can only be a valid scalar or an array of valid scalars",
                Some(entry.value.value)
              )
              AmfArray(Seq())
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

object RamlSpecParserContext extends SpecParserContext {

  override def link(node: YNode): Either[String, YNode] = {
    node match {
      case _ if isInclude(node) => Left(node.value.toScalar.text)
      case _                    => Right(node)
    }
  }

  private def isInclude(node: YNode) = {
    node.tagType == YType.Unknown && node.tag.text == "!include"
  }

  override val vendor: Vendor = Raml
}
