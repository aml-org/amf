package amf.spec.raml

import amf.common.Lazy
import amf.compiler.Root
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation._
import amf.domain._
import amf.domain.extensions.CustomDomainProperty
import amf.domain.security._
import amf.metadata.document.BaseUnitModel
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain.OperationModel.Method
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.metadata.domain.security._
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.spec.common._
import amf.spec.{BaseUriSplitter, Declarations}
import amf.vocabulary.VocabularyMappings
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Raml 1.0 spec parser
  */
case class RamlDocumentParser(override val root: Root) extends RamlSpecParser(root) {

  def parseDocument(): Document = {

    val document = Document().adopted(root.location)

    root.document.value.foreach(value => {
      val map = value.toMap

      val references = ReferencesParser("uses", map, root.references).parse()
      parseDeclarations(map, references.declarations)

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

    map.key("title", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Name, value.string(), Annotations(entry))
    })

    map.key(
      "baseUriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          RamlParametersParser(entry.value.value.toMap, api.withBaseUriParameter, declarations)
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
        val value: AmfElement = entry.value.value match {
          case _: YScalar =>
            annotations += SingleValueArray()
            AmfArray(Seq(ValueNode(entry.value).string()))
          case _: YSequence =>
            ArrayNode(entry.value.value.toSequence).strings()
        }

        api.set(WebApiModel.ContentType, value, annotations)
        api.set(WebApiModel.Accepts, value, annotations)
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
        }
      }
    )

    map.key(
      "(contact)",
      entry => {
        val organization: Organization = OrganizationParser(entry.value.value.toMap).parse()
        api.set(WebApiModel.Provider, organization, Annotations(entry))
      }
    )

    map.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value.value.toMap).parse()
        api.set(WebApiModel.Documentation, creativeWork, Annotations(entry))
      }
    )

    map.key(
      "(license)",
      entry => {
        val license: License = LicenseParser(entry.value.value.toMap).parse()
        api.set(WebApiModel.License, license, Annotations(entry))
      }
    )

    map.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry => EndpointParser(entry, api.withEndPoint, None, endpoints, declarations).parse())
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
          entry.value.asSeq.map(s => ParametrizedSecuritySchemeParser(s, api.withSecurity, declarations).parse())

        api.set(WebApiModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    AnnotationParser(() => api, map).parse()

    api
  }

  case class ParametrizedSecuritySchemeParser(s: YNode,
                                              producer: String => ParametrizedSecurityScheme,
                                              declarations: Declarations) {
    def parse(): ParametrizedSecurityScheme = s.tagType match {
      case YType.Str =>
        val name   = s.asString
        val scheme = producer(name).add(Annotations(s))

        declarations.findSecurityScheme(name) match {
          case Some(declaration) => scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration.id)
          case None if !name.equals("null") =>
            throw new Exception(s"Security scheme '$name' not found in declarations.")
        }

      case YType.Map =>
        val schemeEntry = s.asMap.head
        val name        = schemeEntry._1.asString
        val scheme      = producer(name).add(Annotations(s))

        declarations.findSecurityScheme(name) match {
          case Some(declaration) =>
            scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration.id)

            val settings = SecuritySettingsParser(schemeEntry._2.value.toMap, declaration.`type`, scheme).parse()

            scheme.set(SecuritySchemeModel.Settings, settings)
          case None =>
            throw new Exception(s"Security scheme '$name' not found in declarations (and name cannot be 'null').")
        }

        scheme
      case _ => throw new Exception(s"Invalid type ${s.tagType}")
    }
  }

  case class EndpointParser(entry: YMapEntry,
                            producer: String => EndPoint,
                            parent: Option[EndPoint],
                            collector: mutable.ListBuffer[EndPoint],
                            declarations: Declarations) {
    def parse(): Unit = {

      val path = parent.map(_.path).getOrElse("") + entry.key.value.toScalar.text

      val endpoint = producer(path).add(Annotations(entry))
      parent.map(p => endpoint.add(ParentEndPoint(p)))

      val map = entry.value.value.toMap

      endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

      map.key("displayName", entry => {
        val value = ValueNode(entry.value)
        endpoint.set(EndPointModel.Name, value.string(), Annotations(entry))
      })

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        endpoint.set(EndPointModel.Description, value.string(), Annotations(entry))
      })

      map.key(
        "uriParameters",
        entry => {
          val parameters: Seq[Parameter] =
            RamlParametersParser(entry.value.value.toMap, endpoint.withParameter, declarations)
              .parse()
              .map(_.withBinding("path"))
          endpoint.set(EndPointModel.UriParameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
        }
      )

      map.key(
        "type",
        entry =>
          ParametrizedDeclarationParser(entry.value.value, endpoint.withResourceType, declarations.resourceTypes)
            .parse()
      )

      map.key(
        "is",
        entry => {
          entry.value.value.toSequence.values.map(value =>
            ParametrizedDeclarationParser(value, endpoint.withTrait, declarations.traits).parse())
        }
      )

      map.regex(
        "get|patch|put|post|delete|options|head",
        entries => {
          val operations = mutable.ListBuffer[Operation]()
          entries.foreach(entry => {
            operations += OperationParser(entry, endpoint.withOperation, declarations).parse()
          })
          endpoint.set(EndPointModel.Operations, AmfArray(operations))
        }
      )

      map.key(
        "securedBy",
        entry => {
          // TODO check for empty array for resolution ?
          val securedBy = entry.value.asSeq.map(s =>
            ParametrizedSecuritySchemeParser(s, endpoint.withSecurity, declarations).parse())

          endpoint.set(EndPointModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
        }
      )

      collector += endpoint

      AnnotationParser(() => endpoint, map).parse()

      map.regex(
        "^/.*",
        entries => {
          entries.foreach(EndpointParser(_, producer, Some(endpoint), collector, declarations).parse())
        }
      )
    }
  }

  case class RequestParser(map: YMap, producer: () => Request, declarations: Declarations) {

    def parse(): Option[Request] = {
      val request = new Lazy[Request](producer)
      map.key(
        "queryParameters",
        entry => {

          val parameters: Seq[Parameter] =
            RamlParametersParser(entry.value.value.toMap, request.getOrCreate.withQueryParameter, declarations)
              .parse()
              .map(_.withBinding("query"))
          request.getOrCreate.set(RequestModel.QueryParameters,
                                  AmfArray(parameters, Annotations(entry.value)),
                                  Annotations(entry))
        }
      )

      map.key(
        "headers",
        entry => {
          val parameters: Seq[Parameter] =
            RamlParametersParser(entry.value.value.toMap, request.getOrCreate.withHeader, declarations)
              .parse()
              .map(_.withBinding("header"))
          request.getOrCreate.set(RequestModel.Headers,
                                  AmfArray(parameters, Annotations(entry.value)),
                                  Annotations(entry))
        }
      )

      map.key(
        "body",
        entry => {
          val payloads = mutable.ListBuffer[Payload]()

          RamlTypeParser(entry, shape => shape.withName("default").adopted(request.getOrCreate.id), declarations)
            .parse()
            .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) // todo

          entry.value.value.toMap
            .regex(
              ".*/.*",
              entries => {
                entries.foreach(entry => {
                  payloads += RamlPayloadParser(entry, producer = request.getOrCreate.withPayload, declarations)
                    .parse()
                })
              }
            )
          if (payloads.nonEmpty)
            request.getOrCreate
              .set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
        }
      )

      AnnotationParser(() => request.getOrCreate, map).parse()

      request.option
    }
  }

  case class OperationParser(entry: YMapEntry, producer: (String) => Operation, declarations: Declarations) {

    def parse(): Operation = {

      val method = entry.key.value.toScalar.text

      val operation = producer(method).add(Annotations(entry))

      val map = entry.value.value.toMap

      operation.set(Method, ValueNode(entry.key).string())

      map.key("displayName", entry => {
        val value = ValueNode(entry.value)
        operation.set(OperationModel.Name, value.string(), Annotations(entry))
      })

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        operation.set(OperationModel.Description, value.string(), Annotations(entry))
      })

      map.key("(deprecated)", entry => {
        val value = ValueNode(entry.value)
        operation.set(OperationModel.Deprecated, value.boolean(), Annotations(entry))
      })

      map.key("(summary)", entry => {
        val value = ValueNode(entry.value)
        operation.set(OperationModel.Summary, value.string(), Annotations(entry))
      })

      map.key(
        "(externalDocs)",
        entry => {
          val creativeWork: CreativeWork = CreativeWorkParser(entry.value.value.toMap).parse()
          operation.set(OperationModel.Documentation, creativeWork, Annotations(entry))
        }
      )

      map.key(
        "protocols",
        entry => {
          val value = ArrayNode(entry.value.value.toSequence)
          operation.set(OperationModel.Schemes, value.strings(), Annotations(entry))
        }
      )

      map.key("(consumes)", entry => {
        val value = ArrayNode(entry.value.value.toSequence)
        operation.set(OperationModel.Accepts, value.strings(), Annotations(entry))
      })

      map.key("(produces)", entry => {
        val value = ArrayNode(entry.value.value.toSequence)
        operation.set(OperationModel.ContentType, value.strings(), Annotations(entry))
      })

      map.key(
        "is",
        entry => {
          val traits = entry.value.value.toSequence.nodes.map(value => {
            ParametrizedDeclarationParser(value.value, operation.withTrait, declarations.traits).parse()
          })
          if (traits.nonEmpty) operation.setArray(DomainElementModel.Extends, traits, Annotations(entry))
        }
      )

      RequestParser(map, () => operation.withRequest(), declarations)
        .parse()
        .map(operation.set(OperationModel.Request, _))

      map.key(
        "responses",
        entry => {
          entry.value.value.toMap.regex(
            "\\d{3}",
            entries => {
              val responses = mutable.ListBuffer[Response]()
              entries.foreach(entry => {
                responses += RamlResponseParser(entry, operation.withResponse, declarations).parse()
              })
              operation.set(OperationModel.Responses,
                            AmfArray(responses, Annotations(entry.value)),
                            Annotations(entry))
            }
          )
        }
      )

      map.key(
        "securedBy",
        entry => {
          // TODO check for empty array for resolution ?
          val securedBy = entry.value.asSeq.map(s =>
            ParametrizedSecuritySchemeParser(s, operation.withSecurity, declarations).parse())

          operation.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
        }
      )

      AnnotationParser(() => operation, map).parse()

      operation
    }
  }
}

abstract class RamlSpecParser(val root: Root) extends BaseSpecParser {

  override implicit val spec: SpecParserContext = RamlSpecParserContext

  protected def parseDeclarations(map: YMap, declarations: Declarations): Unit = {
    val parent = root.location + "#/declarations"
    parseTypeDeclarations(map, parent, declarations)
    parseAnnotationTypeDeclarations(map, parent, declarations)
    parseResourceTypeDeclarations("resourceTypes", map, parent, declarations)
    parseTraitDeclarations("traits", map, parent, declarations)
    parseSecuritySchemeDeclarations(map, parent, declarations)
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
          RamlTypeParser(entry, shape => shape.withName(entry.key).adopted(parent), declarations).parse() match {
            case Some(shape) => declarations += shape.add(DeclaredElement())
            case None        => throw new Exception(s"Error parsing shape '$entry'")
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
                                               declarations).parse().add(DeclaredElement())
        }
      }
    )
  }

  case class SecuritySchemeParser(entry: YMapEntry,
                                  adopt: (SecurityScheme) => SecurityScheme,
                                  declarations: Declarations) {
    def parse(): SecurityScheme = {
      spec.link(entry.value) match {
        case Left(link) => parseReferenced(entry.key, link, Annotations(entry.value))
        case Right(value) =>
          val scheme = adopt(SecurityScheme(entry))

          val map = value.value.toMap

          map.key("type", entry => {
            val value = ValueNode(entry.value)
            scheme.set(SecuritySchemeModel.Type, value.string(), Annotations(entry))
          })

          map.key("displayName", entry => {
            val value = ValueNode(entry.value)
            scheme.set(SecuritySchemeModel.DisplayName, value.string(), Annotations(entry))
          })

          map.key("description", entry => {
            val value = ValueNode(entry.value)
            scheme.set(SecuritySchemeModel.Description, value.string(), Annotations(entry))
          })

          map.key(
            "describedBy",
            entry => {
              val value = entry.value.value.toMap

              value.key(
                "headers",
                entry => {
                  val parameters: Seq[Parameter] =
                    RamlParametersParser(entry.value.value.toMap, scheme.withHeader, declarations)
                      .parse()
                      .map(_.withBinding("header"))
                  scheme.set(SecuritySchemeModel.Headers,
                             AmfArray(parameters, Annotations(entry.value)),
                             Annotations(entry))
                }
              )

              value.key(
                "queryParameters",
                entry => {
                  val parameters: Seq[Parameter] =
                    RamlParametersParser(entry.value.value.toMap, scheme.withQueryParameter, declarations)
                      .parse()
                      .map(_.withBinding("query"))
                  scheme.set(SecuritySchemeModel.QueryParameters,
                             AmfArray(parameters, Annotations(entry.value)),
                             Annotations(entry))
                }
              )

              // TODO queryString.

              value.key(
                "responses",
                entry => {
                  entry.value.value.toMap.regex(
                    "\\d{3}",
                    entries => {
                      val responses = mutable.ListBuffer[Response]()
                      entries.foreach(entry => {
                        responses += RamlResponseParser(entry, scheme.withResponse, declarations).parse()
                      })
                      scheme.set(SecuritySchemeModel.Responses,
                                 AmfArray(responses, Annotations(entry.value)),
                                 Annotations(entry))
                    }
                  )
                }
              )
            }
          )

          map.key(
            "settings",
            entry => {
              val settings = SecuritySettingsParser(entry.value.value.toMap, scheme.`type`, scheme).parse()

              scheme.set(SecuritySchemeModel.Settings, settings, Annotations(entry))
            }
          )

          AnnotationParser(() => scheme, map).parse()

          scheme
      }
    }

    def parseReferenced(name: String, parsedUrl: String, annotations: Annotations): SecurityScheme = {
      val declared = declarations.findSecurityScheme(parsedUrl)
      declared
        .map { ss =>
          val copied: SecurityScheme = ss.link(parsedUrl, annotations)
          copied.withName(name)
        }
        .getOrElse(throw new IllegalStateException(s"Could not find security scheme in references map to link $name"))
    }
  }

  case class SecuritySettingsParser(map: YMap, `type`: String, scheme: WithSettings) {
    def parse(): Settings = {
      val result = `type` match {
        case "OAuth 1.0" => oauth1()
        case "OAuth 2.0" => oauth2()
        case "x-apiKey"  => apiKey()
        case _           => dynamicSettings(scheme.withDefaultSettings())
      }

      AnnotationParser(() => result, map).parse()

      result.add(Annotations(map))
    }

    def dynamicSettings(settings: Settings, properties: String*): Settings = {
      val entries = map.entries.filterNot { entry =>
        val key: String = entry.key
        properties.contains(key) || WellKnownAnnotation.isRamlAnnotation(key)
      }

      if (entries.nonEmpty) {
        val node = DataNodeParser(YNode(YMap(entries)), parent = Some(settings.id)).parse()
        settings.set(SettingsModel.AdditionalProperties, node)
      }
      settings
    }

    private def apiKey() = {
      val s = scheme.withApiKeySettings()
      map.key("name", entry => {
        val value = ValueNode(entry.value)
        s.set(ApiKeySettingsModel.Name, value.string(), Annotations(entry))
      })

      map.key("in", entry => {
        val value = ValueNode(entry.value)
        s.set(ApiKeySettingsModel.In, value.string(), Annotations(entry))
      })

      dynamicSettings(s, "name", "in")
    }

    private def oauth2() = {
      val settings = scheme.withOAuth2Settings()
      map.key("authorizationUri", entry => {
        val value = ValueNode(entry.value)
        settings.set(OAuth2SettingsModel.AuthorizationUri, value.string(), Annotations(entry))
      })

      map.key("accessTokenUri", entry => {
        val value = ValueNode(entry.value)
        settings.set(OAuth2SettingsModel.AccessTokenUri, value.string(), Annotations(entry))
      })

      map.key("(flow)", entry => {
        val value = ValueNode(entry.value)
        settings.set(OAuth2SettingsModel.Flow, value.string(), Annotations(entry))
      })

      map.key(
        "authorizationGrants",
        entry => {
          val value = ArrayNode(entry.value.value.toSequence)
          settings.set(OAuth2SettingsModel.AuthorizationGrants, value.strings(), Annotations(entry))
        }
      )

      map.key(
        "scopes",
        entry => {
          val value = ArrayNode(entry.value.value.toSequence)
            .strings()
            .values
            .map(v => Scope().set(ScopeModel.Name, v).adopted(scheme.id))
          settings.setArray(OAuth2SettingsModel.Scopes, value, Annotations(entry))
        }
      )

      dynamicSettings(settings, "authorizationUri", "accessTokenUri", "authorizationGrants", "scopes")
    }

    private def oauth1() = {
      val settings = scheme.withOAuth1Settings()
      map.key("requestTokenUri", entry => {
        val value = ValueNode(entry.value)
        settings.set(OAuth1SettingsModel.RequestTokenUri, value.string(), Annotations(entry))
      })

      map.key("authorizationUri", entry => {
        val value = ValueNode(entry.value)
        settings.set(OAuth1SettingsModel.AuthorizationUri, value.string(), Annotations(entry))
      })

      map.key("tokenCredentialsUri", entry => {
        val value = ValueNode(entry.value)
        settings.set(OAuth1SettingsModel.TokenCredentialsUri, value.string(), Annotations(entry))
      })

      map.key("signatures", entry => {
        val value = ArrayNode(entry.value.value.toSequence)
        settings.set(OAuth1SettingsModel.Signatures, value.strings(), Annotations(entry))
      })

      dynamicSettings(settings, "requestTokenUri", "authorizationUri", "tokenCredentialsUri", "signatures")
    }
  }

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key("usage", entry => {
        val value = ValueNode(entry.value)
        baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
      })
    }
  }

  case class UserDocumentationsParser(map: YMap) {
    def parse(): Seq[UserDocumentation] = {
      val results = ListBuffer[UserDocumentation]()

      map.key("documentation", seq => {
        seq.value.value.toSequence.values.foreach(value => results += UserDocumentationParser(value.toMap).parse())
      })
      results
    }
  }

  case class UserDocumentationParser(map: YMap) {
    def parse(): UserDocumentation = {

      val documentation = UserDocumentation(Annotations(map))

      map.key("title", entry => {
        val value = ValueNode(entry.value)
        documentation.set(UserDocumentationModel.Title, value.string(), Annotations(entry))
      })

      map.key("content", entry => {
        val value = ValueNode(entry.value)
        documentation.set(UserDocumentationModel.Content, value.string(), Annotations(entry))
      })
      documentation
    }
  }

  object AnnotationTypesParser {
    def apply(ast: YMapEntry,
              adopt: (CustomDomainProperty) => Unit,
              declarations: Declarations): CustomDomainProperty =
      ast.value.value match {
        case map: YMap => AnnotationTypesParser(ast, ast.key.value.toScalar.text, map, adopt, declarations).parse()
        case scalar: YScalar =>
          LinkedAnnotationTypeParser(ast, ast.key.value.toScalar.text, scalar, adopt, declarations).parse()
        case _ => throw new IllegalArgumentException("Invalid value Ypart type for annotation types parser")
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
        .getOrElse(throw new UnsupportedOperationException("Could not find declared annotation link in references"))
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
        "type",
        entry => {
          RamlTypeParser(entry, shape => shape.adopted(custom.id), declarations)
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
}
