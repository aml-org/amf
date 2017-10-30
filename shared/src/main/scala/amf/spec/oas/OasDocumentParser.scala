package amf.spec.oas

import amf.common.Lazy
import amf.compiler.Root
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation.{DeclaredElement, DefaultPayload, EndPointBodyParameter, ExplicitField, SingleValueArray, _}
import amf.domain._
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.domain.security._
import amf.metadata.document.BaseUnitModel
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.metadata.domain.security._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.YMapOps
import amf.parser.YValueOps
import amf.remote.{Oas, Vendor}
import amf.shape.NodeShape
import amf.spec.Declarations
import amf.spec.common._
import amf.vocabulary.VocabularyMappings
import org.yaml.model.{YNode, _}
import amf.spec.OasDefinitions
import amf.spec.declaration._
import amf.spec.domain._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Oas 2.0 spec parser
  */
case class OasDocumentParser(root: Root) extends OasSpecParser with OasSyntax {

  def parseDocument(): Document = {

    val document = Document().adopted(root.location)

    root.document.value.foreach(value => {
      val map = value.toMap

      val references = ReferencesParser("x-uses", map, root.references).parse()
      parseDeclarations(root: Root, map, references.declarations)

      val api = parseWebApi(map, references.declarations).add(SourceVendor(root.vendor))
      document
        .withEncodes(api)
        .adopted(root.location)

      val declarable = references.declarations.declarables()
      if (declarable.nonEmpty) document.withDeclares(declarable)
      if (references.references.nonEmpty) document.withReferences(references.solvedReferences())
    })
    document
  }

  def parseWebApi(map: YMap, declarations: Declarations): WebApi = {

    val api = WebApi(map).adopted(root.location)

    map.key(
      "info",
      entry => {
        val info = entry.value.value.toMap

        validateClosedShape(api.id, info, "info")

        info.key("title", entry => {
          val value = ValueNode(entry.value)
          api.set(WebApiModel.Name, value.string(), Annotations(entry))
        })

        info.key("description", entry => {
          val value = ValueNode(entry.value)
          api.set(WebApiModel.Description, value.string(), Annotations(entry))
        })

        info.key("termsOfService", entry => {
          val value = ValueNode(entry.value)
          api.set(WebApiModel.TermsOfService, value.string(), Annotations(entry))
        })

        info.key("version", entry => {
          val value = ValueNode(entry.value)
          api.set(WebApiModel.Version, value.string(), Annotations(entry))
        })

        info.key(
          "contact",
          entry => {
            val organization: Organization = OrganizationParser(entry.value.value.toMap).parse()
            api.set(WebApiModel.Provider, organization, Annotations(entry))
          }
        )

        info.key(
          "license",
          entry => {
            val license: License = LicenseParser(entry.value.value.toMap).parse()
            api.set(WebApiModel.License, license, Annotations(entry))
          }
        )
      }
    )

    map.key("host", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Host, value.string(), Annotations(entry))
    })

    map.key(
      "x-base-uri-parameters",
      entry => {
        val uriParameters =
          HeaderParametersParser(entry.value.value.toMap, api.withBaseUriParameter, declarations).parse()
        api.set(WebApiModel.BaseUriParameters, AmfArray(uriParameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "basePath",
      entry => {
        val value = ValueNode(entry.value)
        api.set(WebApiModel.BasePath, value.string(), Annotations(entry))
      }
    )

    map.key("consumes", entry => {
      val value = ArrayNode(entry.value.value.toSequence)
      api.set(WebApiModel.Accepts, value.strings(), Annotations(entry))
    })

    map.key("produces", entry => {
      val value = ArrayNode(entry.value.value.toSequence)
      api.set(WebApiModel.ContentType, value.strings(), Annotations(entry))
    })

    map.key("schemes", entry => {
      val value = ArrayNode(entry.value.value.toSequence)
      api.set(WebApiModel.Schemes, value.strings(), Annotations(entry))
    })

    val documentations = ListBuffer[CreativeWork]()
    map.key(
      "externalDocs",
      entry => {
        documentations += OasCreativeWorkParser(entry.value.value.toMap).parse()
      }
    )

    map.key(
      "security",
      entry => {
        // TODO check for empty array for resolution ?
        val securedBy =
          entry.value.value.toSequence.values
            .map(s => ParametrizedSecuritySchemeParser(s, api.withSecurity, declarations).parse())

        api.set(WebApiModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "x-user-documentation",
      entry => {
        documentations ++= UserDocumentationParser(entry.value.value.toSequence, declarations, withExtention = false)
          .parse()
      }
    )

    if (documentations.nonEmpty)
      api.setArray(WebApiModel.Documentations, documentations)

    map.key(
      "paths",
      entry => {
        val paths = entry.value.value.toMap
        paths.regex(
          "^/.*",
          entries => {
            val endpoints = mutable.ListBuffer[EndPoint]()
            entries.foreach(EndpointParser(_, api.withEndPoint, endpoints, declarations).parse())
            api.set(WebApiModel.EndPoints, AmfArray(endpoints), Annotations(entry.value))
          }
        )
      }
    )

    AnnotationParser(() => api, map).parse()

    validateClosedShape(api.id, map, "webApi")

    api
  }

  case class ParametrizedSecuritySchemeParser(value: YValue,
                                              producer: String => ParametrizedSecurityScheme,
                                              declarations: Declarations) {
    def parse(): ParametrizedSecurityScheme = value match {
      case map: YMap =>
        val schemeEntry = map.entries.head
        val name        = schemeEntry.key
        val scheme      = producer(name).add(Annotations(map))

        var declaration = parseTarget(name, scheme)
        declaration = declaration.linkTarget match {
          case Some(d) => d.asInstanceOf[SecurityScheme]
          case None    => declaration
        }

        if (declaration.`type` == "OAuth 2.0") {
          val settings = OAuth2Settings().adopted(scheme.id)
          val scopes = schemeEntry.value.value.toSequence.nodes.map(n =>
            Scope(n).set(ScopeModel.Name, AmfScalar(n.as[String]), Annotations(n)))

          scheme.set(ParametrizedSecuritySchemeModel.Settings,
                     settings.setArray(OAuth2SettingsModel.Scopes, scopes, Annotations(schemeEntry.value.value)))
        }

        scheme
      case other => {
        val scheme = producer(other.toString)
        parsingErrorReport(scheme.id, s"Invalid type $other", Some(other))
        scheme
      }
    }

    private def parseTarget(name: String, scheme: ParametrizedSecurityScheme): SecurityScheme = {
      declarations.findSecurityScheme(name) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration.id)
          declaration
        case None =>
          val securityScheme = SecurityScheme()
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, securityScheme)
          parsingErrorReport(securityScheme.id, s"Security scheme '$name' not found in declarations.", None)
          securityScheme
      }
    }
  }

  case class EndpointParser(entry: YMapEntry,
                            producer: String => EndPoint,
                            collector: mutable.ListBuffer[EndPoint],
                            declarations: Declarations) {

    def parse(): Unit = {

      val endpoint = producer(ValueNode(entry.key).string().value.toString).add(Annotations(entry))
      val map      = entry.value.value.toMap

      validateClosedShape(endpoint.id, map, "pathItem")

      map.key("x-displayName", entry => {
        val value = ValueNode(entry.value)
        endpoint.set(EndPointModel.Name, value.string(), Annotations(entry))
      })

      map.key("x-description", entry => {
        val value = ValueNode(entry.value)
        endpoint.set(EndPointModel.Description, value.string(), Annotations(entry))
      })

      var parameters = OasParameters()
      val entries    = ListBuffer[YMapEntry]()
      map
        .key("parameters")
        .foreach(
          entry => {
            entries += entry
            parameters = parameters.addFromOperation(
              ParametersParser(entry.value.value.toSequence, endpoint.id, declarations).parse())
            parameters.body.foreach(_.add(EndPointBodyParameter()))
          }
        )

      map
        .key("x-queryParameters")
        .foreach(
          entry => {
            entries += entry
            val queryParameters =
              RamlParametersParser(entry.value.value.toMap,
                                   (name: String) => Parameter().withName(name).adopted(endpoint.id),
                                   declarations).parse().map(_.withBinding("query"))
            parameters = parameters.addFromOperation(OasParameters(query = queryParameters))
          }
        )

      map
        .key("x-headers")
        .foreach(
          entry => {
            entries += entry
            val headers =
              RamlParametersParser(entry.value.value.toMap,
                                   (name: String) => Parameter().withName(name).adopted(endpoint.id),
                                   declarations).parse().map(_.withBinding("header"))
            parameters = parameters.addFromOperation(OasParameters(header = headers))
          }
        )

      parameters match {
        case OasParameters(_, path, _, _) if path.nonEmpty =>
          endpoint.set(EndPointModel.UriParameters, AmfArray(path, Annotations(entry.value)), Annotations(entry))
        case _ =>
      }

      map.key(
        "x-type",
        entry =>
          ParametrizedDeclarationParser(entry.value.value, endpoint.withResourceType, declarations.resourceTypes)
            .parse()
      )

      map.key(
        "x-is",
        entry => {
          entry.value.value.toSequence.values.map(value =>
            ParametrizedDeclarationParser(value, endpoint.withTrait, declarations.traits).parse())
        }
      )

      collector += endpoint

      AnnotationParser(() => endpoint, map).parse()

      map.key(
        "x-security",
        entry => {
          // TODO check for empty array for resolution ?
          val securedBy = entry.value.value.toSequence.values
            .map(s => ParametrizedSecuritySchemeParser(s, endpoint.withSecurity, declarations).parse())

          endpoint.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
        }
      )

      map.regex(
        "get|patch|put|post|delete|options|head",
        entries => {
          val operations = mutable.ListBuffer[Operation]()
          entries.foreach(entry => {
            operations += OperationParser(entry, parameters, endpoint.withOperation, declarations).parse()
          })
          endpoint.set(EndPointModel.Operations, AmfArray(operations))
        }
      )
    }
  }

  case class RequestParser(map: YMap, globalOrig: OasParameters, producer: () => Request, declarations: Declarations) {
    def parse(): Option[Request] = {
      val request = new Lazy[Request](producer)

      // we remove the path parameters to the empty becase the request
      // can overwrite the path parameters and this would be lost if were not
      // adding them here
      var parameters = globalOrig.copy(path = Seq())
      val global     = globalOrig.copy(path = Seq())
      var entries    = ListBuffer[YMapEntry]()

      val parsedParameters = ListBuffer[Parameter]()
      map
        .key("parameters")
        .foreach(
          entry => {
            entries += entry
            parameters = parameters.merge(
              ParametersParser(entry.value.value.toSequence, request.getOrCreate.id, declarations).parse())
          }
        )

      map
        .key("x-queryParameters")
        .foreach(
          entry => {
            entries += entry
            val queryParameters =
              RamlParametersParser(entry.value.value.toMap,
                                   (name: String) => Parameter().withName(name).adopted(request.getOrCreate.id),
                                   declarations).parse().map(_.withBinding("query"))
            parameters = parameters.addFromOperation(OasParameters(query = queryParameters))
          }
        )

      map
        .key("x-headers")
        .foreach(
          entry => {
            entries += entry
            val headers =
              RamlParametersParser(entry.value.value.toMap,
                                   (name: String) => Parameter().withName(name).adopted(request.getOrCreate.id),
                                   declarations).parse().map(_.withBinding("header"))
            parameters = parameters.addFromOperation(OasParameters(header = headers))
          }
        )

      parameters match {
        case OasParameters(query, path, header, _) =>
          // query parameters and overwritten path parameters
          if (query.nonEmpty || path.nonEmpty)
            request.getOrCreate.set(RequestModel.QueryParameters,
                                    AmfArray(query ++ path, Annotations(entries.head)),
                                    Annotations(entries.head))
          if (header.nonEmpty)
            request.getOrCreate.set(RequestModel.Headers,
                                    AmfArray(header, Annotations(entries.head)),
                                    Annotations(entries.head))
      }

      val payloads = mutable.ListBuffer[Payload]()
      parameters.body.foreach(payloads += _)

      map.key(
        "x-request-payloads",
        entry =>
          entry.value.value.toSequence.values.map(value =>
            payloads += PayloadParser(value.toMap, request.getOrCreate.withPayload, declarations).parse())
      )

      if (payloads.nonEmpty) request.getOrCreate.set(RequestModel.Payloads, AmfArray(payloads))

      map.key(
        "x-queryString",
        queryEntry => {
          RamlTypeParser(queryEntry, (shape) => shape.adopted(request.getOrCreate.id), declarations)
            .parse()
            .map(request.getOrCreate.withQueryString(_))
        }
      )

      AnnotationParser(() => request.getOrCreate, map).parse()

      request.option
    }
  }

  case class OperationParser(entry: YMapEntry,
                             global: OasParameters,
                             producer: String => Operation,
                             declarations: Declarations) {
    def parse(): Operation = {

      val operation = producer(ValueNode(entry.key).string().value.toString).add(Annotations(entry))
      val map       = entry.value.value.toMap

      map.key("operationId", entry => {
        val value = ValueNode(entry.value)
        operation.set(OperationModel.Name, value.string(), Annotations(entry))
      })

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        operation.set(OperationModel.Description, value.string(), Annotations(entry))
      })

      map.key("deprecated", entry => {
        val value = ValueNode(entry.value)
        operation.set(OperationModel.Deprecated, value.boolean(), Annotations(entry))
      })

      map.key("summary", entry => {
        val value = ValueNode(entry.value)
        operation.set(OperationModel.Summary, value.string(), Annotations(entry))
      })

      map.key(
        "externalDocs",
        entry => {
          val creativeWork: CreativeWork = OasCreativeWorkParser(entry.value.value.toMap).parse()
          operation.set(OperationModel.Documentation, creativeWork, Annotations(entry))
        }
      )

      map.key(
        "schemes",
        entry => {
          val value = ArrayNode(entry.value.value.toSequence)
          operation.set(OperationModel.Schemes, value.strings(), Annotations(entry))
        }
      )

      map.key("consumes", entry => {
        val value = ArrayNode(entry.value.value.toSequence)
        operation.set(OperationModel.Accepts, value.strings(), Annotations(entry))
      })

      map.key("produces", entry => {
        val value = ArrayNode(entry.value.value.toSequence)
        operation.set(OperationModel.ContentType, value.strings(), Annotations(entry))
      })

      map.key(
        "x-is",
        entry => {
          val traits = entry.value.value.toSequence.nodes.map(value => {
            ParametrizedDeclarationParser(value.value, operation.withTrait, declarations.traits).parse()
          })
          if (traits.nonEmpty) operation.setArray(DomainElementModel.Extends, traits, Annotations(entry))
        }
      )

      map.key(
        "security",
        entry => {
          // TODO check for empty array for resolution ?
          val securedBy = entry.value.value.toSequence.values
            .map(s => ParametrizedSecuritySchemeParser(s, operation.withSecurity, declarations).parse())

          operation.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
        }
      )

      RequestParser(map, global, () => operation.withRequest(), declarations)
        .parse()
        .map(operation.set(OperationModel.Request, _))

      map.key(
        "responses",
        entry => {
          entry.value.value.toMap.regex(
            "default|\\d{3}",
            entries => {
              val responses = mutable.ListBuffer[Response]()
              entries.foreach(entry => {
                responses += ResponseParser(entry, operation.withResponse, declarations).parse()
              })
              operation.set(OperationModel.Responses,
                            AmfArray(responses, Annotations(entry.value)),
                            Annotations(entry))
            }
          )
        }
      )

      AnnotationParser(() => operation, map).parse()

      validateClosedShape(operation.id, map, "operation")

      operation
    }
  }

  case class ResponseParser(entry: YMapEntry, producer: String => Response, declarations: Declarations) {
    def parse(): Response = {

      val map = entry.value.value.toMap

      val node     = ValueNode(entry.key)
      val response = producer(node.string().value.toString).add(Annotations(entry))

      if (response.name == "default") {
        response.set(ResponseModel.StatusCode, "200")
      } else {
        response.set(ResponseModel.StatusCode, node.string())
      }

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        response.set(ResponseModel.Description, value.string(), Annotations(entry))
      })

      map.key(
        "headers",
        entry => {
          val parameters: Seq[Parameter] =
            HeaderParametersParser(entry.value.value.toMap, response.withHeader, declarations).parse()
          response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
        }
      )

      val payloads = mutable.ListBuffer[Payload]()

      defaultPayload(map, response.id).foreach(payloads += _)

      map.key(
        "x-response-payloads",
        entry =>
          entry.value.value.toSequence.values.map(value =>
            payloads += PayloadParser(value.toMap, response.withPayload, declarations).parse())
      )

      if (payloads.nonEmpty)
        response.set(ResponseModel.Payloads, AmfArray(payloads))

      AnnotationParser(() => response, map).parse()

      validateClosedShape(response.id, map, "response")

      response
    }

    private def defaultPayload(entries: YMap, parentId: String): Option[Payload] = {
      val payload = Payload().add(DefaultPayload())

      entries.key("x-media-type",
                  entry => payload.set(PayloadModel.MediaType, ValueNode(entry.value).string(), Annotations(entry)))
      // TODO add parent id to payload?
      payload.adopted(parentId)

      entries.key(
        "schema",
        entry =>
          OasTypeParser(entry, (shape) => shape.withName("default").adopted(payload.id), declarations)
            .parse()
            .map(payload.set(PayloadModel.Schema, _, Annotations(entry)))
      )

      if (payload.fields.nonEmpty) Some(payload) else None
    }
  }

  case class PayloadParser(map: YMap, producer: (Option[String]) => Payload, declarations: Declarations) {
    def parse(): Payload = {

      val payload = producer(
        map.key("mediaType").map(entry => ValueNode(entry.value).string().value.toString)
      ).add(Annotations(map))

      // todo set again for not lose annotations?
      map.key("mediaType",
              entry => payload.set(PayloadModel.MediaType, ValueNode(entry.value).string(), Annotations(entry)))

      map.key(
        "schema",
        entry => {
          OasTypeParser(entry, (shape) => shape.withName("schema").adopted(payload.id), declarations)
            .parse()
            .map(payload.set(PayloadModel.Schema, _, Annotations(entry)))
        }
      )

      AnnotationParser(() => payload, map).parse()

      payload
    }
  }
}

abstract class OasSpecParser extends BaseSpecParser {

  override implicit val spec: SpecParserContext = OasSpecParserContext

  protected def parseDeclarations(root: Root, map: YMap, declarations: Declarations): Unit = {
    val parent = root.location + "#/declarations"
    parseTypeDeclarations(map, parent, declarations)
    parseAnnotationTypeDeclarations(map, parent, declarations)
    AbstractDeclarationsParser("x-resourceTypes", (entry: YMapEntry) => ResourceType(entry), map, parent, declarations)
      .parse()
    AbstractDeclarationsParser("x-traits", (entry: YMapEntry) => Trait(entry), map, parent, declarations).parse()
    parseSecuritySchemeDeclarations(map, parent, declarations)
    parseParameterDeclarations("parameters", map, parent, declarations)
    declarations.resolve()
  }

  def parseAnnotationTypeDeclarations(map: YMap, customProperties: String, declarations: Declarations): Unit = {

    map.key(
      "x-annotationTypes",
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

  def parseTypeDeclarations(map: YMap, typesPrefix: String, declarations: Declarations): Unit = {

    map.key(
      "definitions",
      entry => {

        entry.value.value.toMap.entries.foreach(e => {
          val typeName = e.key.value.toScalar.text
          OasTypeParser(e, shape => shape.withName(typeName).adopted(typesPrefix), declarations)
            .parse() match {
            case Some(shape) =>
              declarations += shape.add(DeclaredElement())
            case None =>
              parsingErrorReport(NodeShape().adopted(typesPrefix).id, s"Error parsing shape at $typeName", Some(e))
          }
        })
      }
    )
  }

  private def parseSecuritySchemeDeclarations(map: YMap, parent: String, declarations: Declarations): Unit = {
    map.key(
      "securityDefinitions",
      e => {
        e.value.value.toMap.entries.foreach { entry =>
          declarations += SecuritySchemeParser(entry,
                                               scheme => scheme.withName(entry.key).adopted(parent),
                                               declarations).parse().add(DeclaredElement())
        }
      }
    )

    map.key(
      "x-securitySchemes",
      e => {
        e.value.value.toMap.entries.foreach { entry =>
          declarations += SecuritySchemeParser(entry,
                                               scheme => scheme.withName(entry.key).adopted(parent),
                                               declarations).parse().add(DeclaredElement())
        }
      }
    )
  }

  def parseParameterDeclarations(key: String, map: YMap, parentPath: String, declarations: Declarations): Unit = {
    map.key(
      "parameters",
      entry => {
        entry.value.value.toMap.entries.foreach(e => {
          val typeName = e.key.value.toScalar.text
          val oasParameter = e.value.value match {
            case m: YMap => ParameterParser(m, parentPath, declarations).parse()
            case _       =>
              val parameter = ParameterParser(YMap(), parentPath, declarations).parse()
              parsingErrorReport(parameter.parameter.id, "Map needed to parse a parameter declaration", Some(e))
              parameter
          }

          val parameter = oasParameter.parameter.withName(typeName).add(DeclaredElement())
          parameter.fields.getValue(ParameterModel.Binding).annotations += ExplicitField()
          declarations.registerParameter(parameter, oasParameter.payload)
        })
      }
    )
  }

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key("x-usage", entry => {
        val value = ValueNode(entry.value)
        baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
      })
    }
  }

  object AnnotationTypesParser {
    def apply(ast: YMapEntry,
              adopt: (CustomDomainProperty) => Unit,
              declarations: Declarations): CustomDomainProperty =
      ast.value.value match {
        case map: YMap       => AnnotationTypesParser(ast, ast.key.value.toScalar.text, map, adopt, declarations).parse()
        case scalar: YScalar => LinkedAnnotationTypeParser(ast, ast.key.value.toScalar.text, scalar, adopt, declarations).parse()
        case _               =>
          val customDomainProperty = CustomDomainProperty().withName(ast.key.value.toScalar.text)
          adopt(customDomainProperty)
          parsingErrorReport(customDomainProperty.id, "Invalid value node type for annotation types parser, expected map or scalar reference", Some(ast.value.value))
          customDomainProperty
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
          val customDomainProperty = CustomDomainProperty().withName(annotationName)
          adopt(customDomainProperty)
          parsingErrorReport(customDomainProperty.id, "Could not find declared annotation link in references", Some(scalar))
          customDomainProperty
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

      map.key("displayName", entry => {
        val value = ValueNode(entry.value)
        custom.set(CustomDomainPropertyModel.DisplayName, value.string(), Annotations(entry))
      })

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        custom.set(CustomDomainPropertyModel.Description, value.string(), Annotations(entry))
      })

      map.key(
        "schema",
        entry => {
          OasTypeParser(entry, shape => shape.adopted(custom.id), declarations)
            .parse()
            .foreach({ shape =>
              custom.set(CustomDomainPropertyModel.Schema, shape, Annotations(entry))
            })
        }
      )

      AnnotationParser(() => custom, map).parse()

      custom
    }
  }

  case class UserDocumentationParser(seq: YSequence, declarations: Declarations, withExtention: Boolean) {
    def parse(): Seq[CreativeWork] =
      seq.nodes.map(n =>
        n.value match {
          case m: YMap => RamlCreativeWorkParser(m, withExtention).parse()
          case s: YScalar =>
            declarations.findDocumentations(s.text) match {
              case Some(doc) => doc.link(s.text, Annotations(n)).asInstanceOf[CreativeWork]
              case _         => {
                val documentation = RamlCreativeWorkParser(YMap(), withExtention).parse()
                parsingErrorReport(documentation.id, s"not supported scalar $s.text for documentation item", Some(n.value))
                documentation
              }
            }
      })
  }

  case class ParameterParser(map: YMap, parentId: String, declarations: Declarations) extends OasSyntax {
    def parse(): OasParameter = {
      map.key("$ref") match {
        case Some(ref) => parseParameterRef(ref, parentId)
        case None =>
          val parameter = OasParameter(map)

          parameter.parameter.set(ParameterModel.Required, value = false)

          map.key("name", entry => {
            val value = ValueNode(entry.value)
            parameter.parameter.set(ParameterModel.Name, value.string(), Annotations(entry))
          })

          map.key("description", entry => {
            val value = ValueNode(entry.value)
            parameter.parameter.set(ParameterModel.Description, value.string(), Annotations(entry))
          })

          map.key(
            "required",
            entry => {
              val value = ValueNode(entry.value)
              parameter.parameter.set(ParameterModel.Required, value.boolean(), Annotations(entry) += ExplicitField())
            }
          )

          map.key("in", entry => {
            val value = ValueNode(entry.value)
            parameter.parameter.set(ParameterModel.Binding, value.string(), Annotations(entry))
          })

          // TODO generate parameter with parent id or adopt
          if (parameter.isBody) {
            parameter.payload.adopted(parentId)
            map.key(
              "schema",
              entry => {
                OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.payload.id), declarations)
                  .parse()
                  .map(parameter.payload.set(PayloadModel.Schema, _, Annotations(entry)))
              }
            )

            map.key("x-media-type", entry => {
              val value = ValueNode(entry.value)
              parameter.payload.set(PayloadModel.MediaType, value.string(), Annotations(entry))
            })

          } else {
            // type
            parameter.parameter.adopted(parentId)

            validateClosedShape(parameter.parameter.id, map, "parameter")

            OasTypeParser(map,
                          "",
                          map,
                          shape => shape.withName("schema").adopted(parameter.parameter.id),
                          declarations,
                          "parameter")
              .parse()
              .map(parameter.parameter.set(ParameterModel.Schema, _, Annotations(map)))
          }

          AnnotationParser(() => parameter.parameter, map).parse()

          parameter
      }
    }

    protected def parseParameterRef(ref: YMapEntry, parentId: String): OasParameter = {
      val refUrl = OasDefinitions.stripParameterDefinitionsPrefix(ref.value)
      declarations.findParameter(refUrl) match {
        case Some(p) =>
          val payload: Payload     = declarations.parameterPayload(p)
          val parameter: Parameter = p.link(refUrl, Annotations(map))
          parameter.withName(refUrl).adopted(parentId)
          OasParameter(parameter, payload)
        case None =>
          val oasParameter = OasParameter(Parameter(YMap()), Payload(YMap()))
          parsingErrorReport(oasParameter.parameter.id, s"Cannot find parameter reference $refUrl", Some(ref))
          oasParameter
      }
    }
  }

  case class ParametersParser(ast: YSequence, parentId: String, declarations: Declarations) {
    def parse(): OasParameters = {
      val parameters = ast.values
        .map(value => ParameterParser(value.toMap, parentId, declarations).parse())

      OasParameters(
        parameters.filter(_.isQuery).map(_.parameter),
        parameters.filter(_.isPath).map(_.parameter),
        parameters.filter(_.isHeader).map(_.parameter),
        parameters.filter(_.isBody).map(_.payload).headOption
      )
    }
  }

  case class OasParameters(query: Seq[Parameter] = Nil,
                           path: Seq[Parameter] = Nil,
                           header: Seq[Parameter] = Nil,
                           body: Option[Payload] = None) {
    def merge(inner: OasParameters): OasParameters = {
      OasParameters(merge(query, inner.query),
                    merge(path, inner.path),
                    merge(header, inner.header),
                    merge(body, inner.body))
    }

    def addFromOperation(inner: OasParameters): OasParameters = {
      OasParameters(add(query, inner.query), add(path, inner.path), add(header, inner.header), add(body, inner.body))
    }

    private def merge(global: Option[Payload], inner: Option[Payload]): Option[Payload] =
      inner.map(_.add(DefaultPayload())).orElse(global.map(_.copy()))

    private def add(global: Option[Payload], inner: Option[Payload]): Option[Payload] =
      inner.map(_.add(DefaultPayload())).orElse(global.map(_.copy()))

    private def merge(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
      val globalMap = global.map(p => p.name -> p.copy().add(Annotation.EndPointParameter())).toMap
      val innerMap  = inner.map(p => p.name  -> p.copy()).toMap

      (globalMap ++ innerMap).values.toSeq
    }

    private def add(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
      val globalMap = global.map(p => p.name -> p).toMap
      val innerMap  = inner.map(p => p.name  -> p).toMap

      (globalMap ++ innerMap).values.toSeq
    }
  }

  case class OasParameter(parameter: Parameter, payload: Payload) {
    def isBody: Boolean   = parameter.isBody
    def isQuery: Boolean  = parameter.isQuery
    def isPath: Boolean   = parameter.isPath
    def isHeader: Boolean = parameter.isHeader
  }

  object OasParameter {
    def apply(ast: YMap): OasParameter = OasParameter(Parameter(ast), Payload(ast))
  }

  case class HeaderParametersParser(map: YMap, producer: String => Parameter, declarations: Declarations) {
    def parse(): Seq[Parameter] = {
      map.entries
        .map(entry => HeaderParameterParser(entry, producer, declarations).parse())
    }
  }

  case class HeaderParameterParser(entry: YMapEntry, producer: String => Parameter, declarations: Declarations) {
    def parse(): Parameter = {

      val name      = entry.key.value.toScalar.text
      val parameter = producer(name).add(Annotations(entry))

      parameter
        .set(ParameterModel.Required, !name.endsWith("?"))
        .set(ParameterModel.Name, ValueNode(entry.key).string())

      val map = entry.value.value.toMap

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        parameter.set(ParameterModel.Description, value.string(), Annotations(entry))
      })

      map.key("required", entry => {
        val value = ValueNode(entry.value)
        parameter.set(ParameterModel.Required, value.boolean(), Annotations(entry) += ExplicitField())
      })

      map.key(
        "type",
        _ => {
          OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.id), declarations)
            .parse()
            .map(parameter.set(ParameterModel.Schema, _, Annotations(entry)))
        }
      )

      AnnotationParser(() => parameter, map).parse()

      parameter
    }
  }

}

object OasSpecParserContext extends SpecParserContext {

  override def link(node: YNode): Either[String, YNode] = {
    node match {
      case map if isMap(map) =>
        val ref: Option[String] = map.value.toMap.key("$ref").map(v => v.value)
        ref match {
          case Some(url) => Left(url)
          case None      => Right(node)
        }
      case _ => Right(node)
    }
  }

  private def isMap(node: YNode) = node.tag.tagType == YType.Map

  override val vendor: Vendor = Oas
}

case class OasParameter(parameter: Parameter, payload: Payload) {
  def isBody: Boolean   = parameter.isBody
  def isQuery: Boolean  = parameter.isQuery
  def isPath: Boolean   = parameter.isPath
  def isHeader: Boolean = parameter.isHeader
}

object OasParameter {
  def apply(ast: YMap): OasParameter = OasParameter(Parameter(ast), Payload(ast))
}
