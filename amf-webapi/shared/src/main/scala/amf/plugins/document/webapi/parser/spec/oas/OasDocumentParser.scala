package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.annotations.{DeclaredElement, SingleValueArray, SourceVendor, SynthesizedField}
import amf.core.metamodel.Field
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{IdCounter, Lazy, Strings, TemplateUri}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.spec
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorCallback
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isOasAnnotation
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, WebApiBaseSpecParser}
import amf.plugins.document.webapi.parser.spec.declaration.{AbstractDeclarationsParser, SecuritySchemeParser, _}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.shapes.models.{CreativeWork, NodeShape}
import amf.plugins.domain.webapi.metamodel.security.{OAuth2SettingsModel, ParametrizedSecuritySchemeModel, ScopeModel}
import amf.plugins.domain.webapi.metamodel.{EndPointModel, _}
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.security._
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations._
import amf.plugins.features.validation.CoreValidations.DeclarationNotFound
import org.yaml.model.{YNode, _}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Oas spec parser
  */
abstract class OasDocumentParser(root: Root)(implicit val ctx: OasWebApiContext) extends OasSpecParser {

  def parseExtension(): Extension = {
    val extension = parseDocument(Extension())

    parseExtension(extension, ExtensionLikeModel.Extends)

    extension
  }

  private def parseExtension(document: Document, field: Field): Unit = {
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    UsageParser(map, document).parse()

    map
      .key("extends".asOasExtension)
      .foreach(e => {
        ctx.link(e.value) match {
          case Left(url) =>
            root.references
              .find(_.origin.url == url)
              .foreach(extend =>
                document
                  .set(field, AmfScalar(extend.unit.id, Annotations(e.value)), Annotations(e)))
          case _ =>
        }
      })
  }

  def parseOverlay(): Overlay = {
    val overlay = parseDocument(Overlay())

    parseExtension(overlay, ExtensionLikeModel.Extends)

    overlay
  }

  def parseDocument(): Document = parseDocument(Document())

  private def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    ctx.setJsonSchemaAST(map)

    val references = ReferencesParser(document, "uses".asOasExtension, map, root.references).parse(root.location)
    parseDeclarations(root: Root, map)

    val api = parseWebApi(map).add(SourceVendor(ctx.vendor))
    document
      .withEncodes(api)
      .adopted(root.location)

    val declarable = ctx.declarations.declarables()
    if (declarable.nonEmpty) document.withDeclares(declarable)
    if (references.references.nonEmpty) document.withReferences(references.solvedReferences())

    ctx.futureDeclarations.resolve()
    document
  }

  def parseDeclarations(root: Root, map: YMap): Unit = {
    val parent = root.location + "#/declarations"
    parseTypeDeclarations(map, parent + "/types")
    parseAnnotationTypeDeclarations(map, parent)
    AbstractDeclarationsParser("resourceTypes".asOasExtension,
                               (entry: YMapEntry) => ResourceType(entry),
                               map,
                               parent + "/resourceTypes").parse()
    AbstractDeclarationsParser("traits".asOasExtension, (entry: YMapEntry) => Trait(entry), map, parent + "/traits")
      .parse()
    parseSecuritySchemeDeclarations(map, parent + "/securitySchemes")
    parseParameterDeclarations(map, parent + "/parameters")
    parseResponsesDeclarations("responses", map, parent + "/responses")
  }

  protected def parseAnnotationTypeDeclarations(map: YMap, customProperties: String): Unit = {

    map.key(
      "annotationTypes".asOasExtension,
      e => {
        e.value
          .as[YMap]
          .entries
          .map(entry => {
            val typeName = entry.key.as[YScalar].text
            val customProperty = AnnotationTypesParser(entry,
                                                       customProperty =>
                                                         customProperty
                                                           .withName(typeName)
                                                           .adopted(customProperties))
            ctx.declarations += customProperty.add(DeclaredElement())
          })
      }
    )
  }

  protected val definitionsKey: String
  protected val securityKey: String

  def parseTypeDeclarations(map: YMap, typesPrefix: String): Unit = {

    map.key(
      definitionsKey,
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val typeName = e.key.as[YScalar].text
            OasTypeParser(e, shape => {
              shape.set(ShapeModel.Name, AmfScalar(typeName, Annotations(e.key.value)), Annotations(e.key))
              shape.adopted(typesPrefix)
            })(ctx).parse() match {
              case Some(shape) =>
                ctx.declarations += shape.add(DeclaredElement())
              case None =>
                ctx.violation(UnableToParseShape,
                              NodeShape().adopted(typesPrefix).id,
                              s"Error parsing shape at $typeName",
                              e)
            }
          })
      }
    )
  }

  protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      securityKey,
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          ctx.declarations += SecuritySchemeParser(
            entry,
            (scheme, name) => {
              scheme.set(ParametrizedSecuritySchemeModel.Name,
                         AmfScalar(name, Annotations(entry.key.value)),
                         Annotations(entry.key))
              scheme.adopted(parent)
            }
          ).parse()
            .add(DeclaredElement())
        }
      }
    )

    map.key(
      "securitySchemes".asOasExtension,
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          ctx.declarations += SecuritySchemeParser(
            entry,
            (scheme, name) => {
              scheme.set(ParametrizedSecuritySchemeModel.Name,
                         AmfScalar(name, Annotations(entry.key.value)),
                         Annotations(entry.key))
              scheme.adopted(parent)
            }
          ).parse()
            .add(DeclaredElement())
        }
      }
    )
  }

  protected def parseParameterDeclarations(map: YMap, parentPath: String): Unit = {
    map.key(
      "parameters",
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val typeName      = e.key
            val nameGenerator = new IdCounter()
            val oasParameter: domain.OasParameter = e.value.to[YMap] match {
              case Right(_) => ctx.factory.parameterParser(Left(e), parentPath, Some(typeName), nameGenerator).parse
              case _ =>
                val parameter =
                  ctx.factory.parameterParser(Right(YMap.empty), parentPath, Some(typeName), nameGenerator).parse
                ctx.violation(InvalidParameterType,
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

  protected def parseResponsesDeclarations(key: String, map: YMap, parentPath: String): Unit = {
    map.key(
      key,
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val node = ScalarNode(e.key).text()
            ctx.declarations += OasResponseParser(e.value.as[YMap], { r: Response =>
              r.set(ResponseModel.Name, node).adopted(parentPath).add(DeclaredElement())
              r.annotations ++= Annotations(e)
            }).parse()
          })
      }
    )
  }

  def parseWebApi(map: YMap): WebApi = {

    val api = WebApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node).adopted(root.location)

    map.key(
      "info",
      entry => {
        val info = entry.value.as[YMap]

        ctx.closedShape(api.id, info, "info")

        info.key("title", WebApiModel.Name in api)
        info.key("description", WebApiModel.Description in api)
        info.key("termsOfService", WebApiModel.TermsOfService in api)
        info.key("version", WebApiModel.Version in api)
        info.key("contact", WebApiModel.Provider in api using OrganizationParser.parse)
        info.key("license", WebApiModel.License in api using LicenseParser.parse)
      }
    )

    ctx.factory.serversParser(map, api).parse()

    map.key(
      "tags",
      entry => {
        entry.value.tagType match {
          case YType.Seq =>
            val tags = entry.value.as[Seq[YMap]].map(tag => TagsParser(tag, (tag: Tag) => tag.adopted(api.id)).parse())
            validateDuplicated(tags, entry)
            api.set(WebApiModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
          case _ => // ignore
        }

      }
    )

    map.key(
      "security",
      entry => {
        entry.value.tagType match {
          case YType.Seq =>
            val securedBy =
              entry.value
                .as[Seq[YNode]]
                .map(s => ParametrizedSecuritySchemeParser(s, api.withSecurity).parse())
                .collect { case Some(s) => s }

            api.set(WebApiModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
          case _ => // ignore
        }
      }
    )

    val documentations = ListBuffer[CreativeWork]()

    map.key(
      "externalDocs",
      entry => {
        documentations += OasCreativeWorkParser(entry.value).parse()
      }
    )

    map.key(
      "userDocumentation".asOasExtension,
      entry => {
        documentations ++= UserDocumentationParser(entry.value.as[Seq[YNode]])
          .parse()
      }
    )

    if (documentations.nonEmpty) api.setArray(WebApiModel.Documentations, documentations)

    map.key(
      "paths",
      entry => {
        val paths = entry.value.as[YMap]
        paths.regex(
          "^/.*",
          entries => {
            val endpoints = mutable.ListBuffer[EndPoint]()
            entries.foreach(EndpointParser(_, api.withEndPoint, endpoints).parse())
            api.set(WebApiModel.EndPoints, AmfArray(endpoints), Annotations(entry.value))
          }
        )

        ctx.closedShape(api.id, paths, "paths")
      }
    )

    AnnotationParser(api, map).parse()
    AnnotationParser(api, map).parseOrphanNode("paths")

    ctx.closedShape(api.id, map, "webApi")

    api
  }

  private def validateDuplicated(tags: Seq[Tag], entry: YMapEntry): Unit = {
    val groupedByName = tags
      .flatMap { tag =>
        tag.name.option().map(_ -> tag)
      }
      .groupBy { case (name, _) => name }
    val namesWithTag = groupedByName.collect { case (_, ys) if ys.lengthCompare(1) > 0 => ys.tail }.flatten
    namesWithTag.foreach {
      case (name, tag) =>
        ctx.violation(DuplicatedTags, tag.id, s"Tag with name '$name' was found duplicated", tag.annotations)
    }
  }

  case class ParametrizedSecuritySchemeParser(node: YNode, producer: String => ParametrizedSecurityScheme) {
    def parse(): Option[ParametrizedSecurityScheme] = node.to[YMap] match {
      case Right(map) if map.entries.nonEmpty =>
        val schemeEntry = map.entries.head
        val name        = schemeEntry.key.as[YScalar].text
        val scheme      = producer(name).add(Annotations(map))

        var declaration = parseTarget(name, scheme, schemeEntry)
        declaration = declaration.linkTarget match {
          case Some(d) => d.asInstanceOf[SecurityScheme]
          case None    => declaration
        }

        if (declaration.`type`.is("OAuth 2.0")) {
          val settings = OAuth2Settings().adopted(scheme.id)
          val scopes = schemeEntry.value
            .as[Seq[YNode]]
            .map(n => Scope(n).set(ScopeModel.Name, AmfScalar(n.as[String]), Annotations(n)))

          scheme.set(ParametrizedSecuritySchemeModel.Settings,
                     settings.setArray(OAuth2SettingsModel.Scopes, scopes, Annotations(schemeEntry.value)))
        }

        Some(scheme)
      case Right(map) if map.entries.isEmpty =>
        None
      case _ =>
        val scheme = producer(node.toString)
        ctx.violation(InvalidSecuredByType, scheme.id, s"Invalid type $node", node)
        None
    }

    private def parseTarget(name: String, scheme: ParametrizedSecurityScheme, part: YPart): SecurityScheme = {
      ctx.declarations.findSecurityScheme(name, SearchScope.All) match {
        case Some(declaration) =>
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, declaration)
          declaration
        case None =>
          val securityScheme = SecurityScheme()
          scheme.set(ParametrizedSecuritySchemeModel.Scheme, securityScheme)
          ctx.violation(DeclarationNotFound,
                        securityScheme.id,
                        s"Security scheme '$name' not found in declarations.",
                        part)
          securityScheme
      }
    }
  }

  case class EndpointParser(entry: YMapEntry, producer: String => EndPoint, collector: mutable.ListBuffer[EndPoint]) {

    def parse(): Unit = {
      val path = entry.key.as[YScalar].text

      val endpoint = producer(path).add(Annotations(entry))

      checkBalancedParams(path, entry.value, endpoint.id, EndPointModel.Path.value.iri(), ctx)
      endpoint.set(EndPointModel.Path, AmfScalar(path, Annotations(entry.key)))

      if (!TemplateUri.isValid(path))
        ctx.violation(InvalidEndpointPath, endpoint.id, TemplateUri.invalidMsg(path), entry.value)

      if (collector.exists(e => e.path.is(path)))
        ctx.violation(DuplicatedEndpointPath, endpoint.id, "Duplicated resource path " + path, entry)
      else parseEndpoint(endpoint)
    }

    private def parseEndpoint(endpoint: EndPoint) =
      ctx.link(entry.value) match {
        case Left(value) =>
          ctx.declarations.asts.get(value) match {
            case Some(n) if n.tagType == YType.Map =>
              parseEndpointMap(endpoint, n.as[YMap])
            case Some(n) =>
              ctx.violation(InvalidEndpointType, endpoint.id, "Invalid node for path item", n)
            case None =>
              ctx.violation(InvalidEndpointPath,
                            endpoint.id,
                            s"Cannot find fragment path item ref $value",
                            entry.value)
          }
        case Right(node) if node.tagType == YType.Map =>
          parseEndpointMap(endpoint, node.as[YMap])
        case _ =>
          collector += endpoint
      }

    private def parseEndpointMap(endpoint: EndPoint, map: YMap): Unit = {
      ctx.closedShape(endpoint.id, map, "pathItem")

      map.key("displayName".asOasExtension, EndPointModel.Name in endpoint)
      map.key("description".asOasExtension, EndPointModel.Description in endpoint)
      map.key("summary", EndPointModel.Summary in endpoint)

      var parameters = Parameters()
      val entries    = ListBuffer[YMapEntry]()

      ctx.factory.serversParser(map, endpoint).parse()

      // This are the rest of the parameters, this must be simple to be supported by OAS.
      map
        .key("parameters")
        .foreach { entry =>
          entries += entry
          parameters = parameters.add(OasParametersParser(entry.value.as[Seq[YNode]], endpoint.id).parse())
        }

      // This is because there may be complex path parameters coming from RAML1
      map.key("uriParameters".asOasExtension).foreach { entry =>
        entries += entry
        val uriParameters =
          RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(endpoint.id))(spec.toRaml(ctx))
            .parse()
            .map(_.withBinding("path"))
        parameters = parameters.add(Parameters(path = uriParameters))
      }

      parameters match {
        case Parameters(query, path, header, cookie, _, _)
            if query.nonEmpty || path.nonEmpty || header.nonEmpty || cookie.nonEmpty =>
          endpoint.set(EndPointModel.Parameters,
                       AmfArray(query ++ path ++ header ++ cookie, Annotations(entries.head.value)),
                       Annotations(entries.head))
        case _ =>
      }

      if (parameters.body.nonEmpty)
        endpoint.set(EndPointModel.Payloads, AmfArray(parameters.body), Annotations(entries.head))

      map.key("is".asOasExtension,
              (EndPointModel.Extends in endpoint using ParametrizedDeclarationParser
                .parse(endpoint.withTrait)).allowingSingleValue)

      map.key(
        "type".asOasExtension,
        entry =>
          ParametrizedDeclarationParser(entry.value,
                                        endpoint.withResourceType,
                                        ctx.declarations.findResourceTypeOrError(entry.value))
            .parse()
      )

      collector += endpoint

      AnnotationParser(endpoint, map).parse()

      map.key(
        "security".asOasExtension,
        entry => {
          // TODO check for empty array for resolution ?
          val securedBy = entry.value
            .as[Seq[YNode]]
            .map(s => ParametrizedSecuritySchemeParser(s, endpoint.withSecurity).parse())
            .collect { case Some(s) => s }

          if (securedBy.nonEmpty)
            endpoint.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
        }
      )

      map.regex(
        "get|patch|put|post|delete|options|head|connect|trace",
        entries => {
          val operations = mutable.ListBuffer[Operation]()
          entries.foreach { entry =>
            operations += OperationParser(entry, endpoint.withOperation).parse()
          }
          endpoint.set(EndPointModel.Operations, AmfArray(operations))
        }
      )
    }
  }

  case class RequestParser(map: YMap, adopt: Request => Unit)(implicit ctx: OasWebApiContext) {
    def parse(): Option[Request] = {
      if (ctx.syntax == Oas3Syntax) {
        Oas3RequestParser(map, adopt).parse()
      } else {
        Oas2RequestParser(map, adopt).parse()
      }
    }
  }

  case class Oas3RequestParser(map: YMap, adopt: Request => Unit)(implicit ctx: OasWebApiContext) {
    def parse(): Option[Request] = {
      ctx.link(map) match {
        case Left(fullRef) =>
          parseRef(fullRef)
        case Right(_) =>
          val request = Request()
          adopt(request)

          map.key("description", RequestModel.Description in request)
          map.key("required", RequestModel.Required in request)

          val payloads = mutable.ListBuffer[Payload]()

          map.key(
            "content",
            entry =>
              entry.value
                .as[YMap]
                .entries
                .foreach { entry =>
                  val mediaType = ScalarNode(entry.key).text().value.toString
                  payloads += OasContentParser(entry.value, mediaType, request.withPayload).parse()
              }
          )
          request.set(ResponseModel.Payloads, AmfArray(payloads))

          AnnotationParser(request, map).parse()
          ctx.closedShape(request.id, map, "request")
          Some(request)
      }
    }

    private def parseRef(fullRef: String): Option[Request] = {
      val name = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "requestBodies")
      ctx.declarations
        .findRequestBody(name, SearchScope.Named)
        .map { req =>
          val linkReq: Request = req.link(name, Annotations(map))
          linkReq.withName(name)
          adopt(linkReq)
          linkReq
        }
        .orElse {
          ctx.obtainRemoteYNode(fullRef) match {
            case Some(requestNode) =>
              Oas3RequestParser(requestNode.as[YMap], adopt).parse()
            case None =>
              ctx.violation(CoreValidations.UnresolvedReference,
                            "",
                            s"Cannot find requestBody reference $fullRef",
                            map)
              None
          }
        }
    }
  }

  case class Oas2RequestParser(map: YMap, adopt: Request => Unit)(implicit ctx: OasWebApiContext) {
    def parse(): Option[Request] = {
      val request = new Lazy[Request](() => {
        val req = Request()
        adopt(req)
        req
      })
      var parameters = Parameters()
      var entries    = ListBuffer[YMapEntry]()

      map
        .key("parameters")
        .foreach { entry =>
          entries += entry
          parameters = parameters.add(
            OasParametersParser(entry.value.as[Seq[YNode]], request.getOrCreate.id).parse(inRequest = true))
        }

      map
        .key("queryParameters".asOasExtension)
        .foreach(
          entry => {
            entries += entry
            val queryParameters =
              RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(request.getOrCreate.id))(
                spec.toRaml(ctx))
                .parse()
                .map(_.withBinding("query"))
            parameters = parameters.add(Parameters(query = queryParameters))
          }
        )

      map
        .key("headers".asOasExtension)
        .foreach(
          entry => {
            entries += entry
            val headers =
              RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(request.getOrCreate.id))(
                spec.toRaml(ctx))
                .parse()
                .map(_.withBinding("header"))
            parameters = parameters.add(Parameters(header = headers))
          }
        )

      // baseUriParameters from raml08. Only complex parameters will be written here, simple ones will be in the parameters with binding path.
      map.key(
        "baseUriParameters".asOasExtension,
        entry => {
          entry.value.as[YMap].entries.headOption.foreach { paramEntry =>
            val parameter =
              Raml08ParameterParser(paramEntry, (p: Parameter) => p.adopted(request.getOrCreate.id))(spec.toRaml(ctx))
                .parse()
                .withBinding("path")
            parameters = parameters.add(Parameters(baseUri08 = Seq(parameter)))
          }
        }
      )

      parameters match {
        case Parameters(query, path, header, _, baseUri08, _) =>
          if (query.nonEmpty)
            request.getOrCreate.set(RequestModel.QueryParameters,
                                    AmfArray(query, Annotations(entries.head)),
                                    Annotations(entries.head))
          if (header.nonEmpty)
            request.getOrCreate.set(RequestModel.Headers,
                                    AmfArray(header, Annotations(entries.head)),
                                    Annotations(entries.head))

          if (path.nonEmpty || baseUri08.nonEmpty)
            request.getOrCreate.set(RequestModel.UriParameters,
                                    AmfArray(path ++ baseUri08, Annotations(entries.head)),
                                    Annotations(entries.head))
      }

      val payloads = mutable.ListBuffer[Payload]()

      parameters.body.foreach(payloads += _)

      map.key(
        "requestPayloads".asOasExtension,
        entry =>
          entry.value
            .as[Seq[YNode]]
            .map(value => payloads += OasPayloadParser(value, request.getOrCreate.withPayload).parse())
      )

      if (payloads.nonEmpty) request.getOrCreate.set(RequestModel.Payloads, AmfArray(payloads))

      map.key(
        "queryString".asOasExtension,
        queryEntry => {
          Raml10TypeParser(queryEntry, shape => shape.adopted(request.getOrCreate.id))(toRaml(ctx))
            .parse()
            .map(s => request.getOrCreate.withQueryString(tracking(s, request.getOrCreate.id)))
        }
      )

      request.option
    }
  }

  case class Oas3ParametersParser(map: YMap, producer: () => Request)(implicit ctx: OasWebApiContext) {

    def parseParameters(): Unit = {
      val request = new Lazy[Request](producer)
      map
        .key("parameters")
        .foreach { entry =>
          val parameters =
            OasParametersParser(entry.value.as[Seq[YNode]], request.getOrCreate.id).parse(inRequest = true)
          parameters match {
            case Parameters(query, path, header, cookie, baseUri08, _) =>
              if (query.nonEmpty)
                request.getOrCreate.set(RequestModel.QueryParameters,
                                        AmfArray(query, Annotations(entry)),
                                        Annotations(entry))
              if (header.nonEmpty)
                request.getOrCreate.set(RequestModel.Headers, AmfArray(header, Annotations(entry)), Annotations(entry))
              if (path.nonEmpty || baseUri08.nonEmpty)
                request.getOrCreate.set(RequestModel.UriParameters,
                                        AmfArray(path ++ baseUri08, Annotations(entry)),
                                        Annotations(entry))
              if (cookie.nonEmpty)
                request.getOrCreate.set(RequestModel.CookieParameters,
                                        AmfArray(cookie, Annotations(entry)),
                                        Annotations(entry))
          }
        }

    }
  }

  case class CallbackParser(map: YMap, adopt: Callback => Unit) {
    def parse(): Callback = {
      ctx.link(map) match {
        case Left(fullRef) =>
          val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "callbacks")
          ctx.declarations
            .findCallback(label, SearchScope.Named)
            .map { callback =>
              val linkCallback: Callback = callback.link(label, Annotations(map))
              adopt(linkCallback)
              linkCallback
            }
            .getOrElse {
              ctx.obtainRemoteYNode(fullRef) match {
                case Some(callbackNode) =>
                  CallbackParser(callbackNode.as[YMap], adopt).parse()
                case None =>
                  ctx.violation(CoreValidations.UnresolvedReference,
                                "",
                                s"Cannot find callback reference $fullRef",
                                map)
                  new ErrorCallback(label, map)
              }
            }
        case Right(_) =>
          val callbackOperations = map.entries
          val callback           = Callback().add(Annotations(map))
          adopt(callback)
          if (callbackOperations.size != 1) {
            // TODO throw violation here
          } else {
            EndpointParser(callbackOperations.head, callback.withEndpoint, mutable.ListBuffer.empty).parse()
          }
          callback
      }

    }
  }

  case class OperationParser(entry: YMapEntry, producer: String => Operation) {
    def parse(): Operation = {

      val operation = producer(ScalarNode(entry.key).string().value.toString).add(Annotations(entry))
      val map       = entry.value.as[YMap]

      // oas 3.0.0 / oas 2.0
      map.key("operationId").foreach { entry =>
        val operationId = entry.value.toString()
        if (!ctx.registerOperationId(operationId))
          ctx.violation(DuplicatedOperationId, operation.id, s"Duplicated operation id '$operationId'", entry.value)
      }

      // oas 3.0.0 / oas 2.0
      map.key("operationId", OperationModel.Name in operation)
      // oas 3.0.0 / oas 2.0
      map.key("description", OperationModel.Description in operation)
      // oas 3.0.0 / oas 2.0
      map.key("deprecated", OperationModel.Deprecated in operation)
      // oas 3.0.0 / oas 2.0
      map.key("summary", OperationModel.Summary in operation)
      // oas 3.0.0 / oas 2.0
      map.key("externalDocs", OperationModel.Documentation in operation using OasCreativeWorkParser.parse)
      // oas 2.0
      if (ctx.syntax == Oas2Syntax) {
        map.key("schemes", OperationModel.Schemes in operation)
        map.key("consumes", OperationModel.Accepts in operation)
        map.key("produces", OperationModel.ContentType in operation)
      }
      // oas 3.0.0 / oas 2.0
      map.key("tags", OperationModel.Tags in operation)
      // oas 3.0.0
      if (ctx.syntax == Oas3Syntax) {
        map.key(
          "requestBody",
          entry => {
            Oas3RequestParser(entry.value.as[YMap], req => operation.withRequest(req)).parse()
          }
        )

        // parameters defined in endpoint are stored in the request
        Oas3ParametersParser(map, Option(operation.request).map(() => _).getOrElse(operation.withRequest))
          .parseParameters()

        map.key(
          "callbacks",
          entry => {
            val callbacks = entry.value
              .as[YMap]
              .entries
              .map { callbackEntry =>
                val name = callbackEntry.key.as[YScalar].text
                CallbackParser(callbackEntry.value.as[YMap], _.withName(name).adopted(operation.id)).parse()
              }
            operation.withCallbacks(callbacks)
          }
        )
        ctx.factory.serversParser(map, operation).parse()
      }

      // oas 3.0.0 / oas 2.0

      map.key(
        "is".asOasExtension,
        entry => {
          val traits = entry.value
            .as[Seq[YNode]]
            .map(value => {
              ParametrizedDeclarationParser(value, operation.withTrait, ctx.declarations.findTraitOrError(value))
                .parse()
            })
          if (traits.nonEmpty) operation.setArray(DomainElementModel.Extends, traits, Annotations(entry))
        }
      )

      map.key(
        "security",
        entry => {
          // TODO check for empty array for resolution ?
          val securedBy = entry.value
            .as[Seq[YNode]]
            .map(s => ParametrizedSecuritySchemeParser(s, operation.withSecurity).parse())
            .collect { case Some(s) => s }

          operation.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
        }
      )

      // oas 2.0
      if (ctx.syntax == Oas2Syntax) {
        RequestParser(map, req => operation.withRequest(req))
          .parse()
          .map(operation.set(OperationModel.Request, _, Annotations() += SynthesizedField()))
      }

      // oas 3.0.0 / oas 2.0
      map.key(
        "responses",
        entry => {
          val responses = mutable.ListBuffer[Response]()

          entry.value
            .as[YMap]
            .entries
            .filter(y => !isOasAnnotation(y.key.as[YScalar].text))
            .foreach { entry =>
              val node = ScalarNode(entry.key).text()
              responses += OasResponseParser(entry.value.as[YMap], { r =>
                r.set(ResponseModel.Name, node)
                  .adopted(operation.id)
                  .withStatusCode(r.name.value())
                r.annotations ++= Annotations(entry)
              }).parse()
            }

          operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), Annotations(entry))
        }
      )

      AnnotationParser(operation, map).parseOrphanNode("responses")
      AnnotationParser(operation, map).parse()

      ctx.closedShape(operation.id, map, "operation")

      operation
    }
  }
}

abstract class OasSpecParser(implicit ctx: OasWebApiContext) extends WebApiBaseSpecParser with SpecParserOps {

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key("usage".asOasExtension, entry => {
        val value = ScalarNode(entry.value)
        baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
      })
    }
  }

  object AnnotationTypesParser {
    def apply(ast: YMapEntry, adopt: CustomDomainProperty => Unit): CustomDomainProperty =
      ast.value.tagType match {
        case YType.Map =>
          ast.value.as[YMap].key("$ref") match {
            case Some(reference) =>
              LinkedAnnotationTypeParser(ast, reference.value.as[YScalar].text, reference.value.as[YScalar], adopt)
                .parse()
            case _ => AnnotationTypesParser(ast, ast.key.as[YScalar].text, ast.value.as[YMap], adopt).parse()
          }
        case YType.Seq =>
          val customDomainProperty = CustomDomainProperty().withName(ast.key.as[YScalar].text)
          adopt(customDomainProperty)
          ctx.violation(
            InvalidAnnotationType,
            customDomainProperty.id,
            "Invalid value node type for annotation types parser, expected map or scalar reference",
            ast.value
          )
          customDomainProperty
        case _ =>
          LinkedAnnotationTypeParser(ast, ast.key.as[YScalar].text, ast.value.as[YScalar], adopt).parse()
      }

  }

  case class LinkedAnnotationTypeParser(ast: YPart,
                                        annotationName: String,
                                        scalar: YScalar,
                                        adopt: CustomDomainProperty => Unit) {
    def parse(): CustomDomainProperty = {
      ctx.declarations
        .findAnnotation(scalar.text, SearchScope.All)
        .map { a =>
          val copied: CustomDomainProperty = a.link(scalar.text, Annotations(ast))
          copied.id = null // we reset the ID so ti can be adopted, there's an extra rule where the id is not set
          // because the way they are inserted in the mode later in the parsing
          adopt(copied.withName(annotationName))
          copied
        }
        .getOrElse {
          val customDomainProperty = CustomDomainProperty().withName(annotationName)
          adopt(customDomainProperty)
          ctx.violation(DeclarationNotFound,
                        customDomainProperty.id,
                        "Could not find declared annotation link in references",
                        scalar)
          customDomainProperty
        }
    }
  }

  case class AnnotationTypesParser(ast: YPart, annotationName: String, map: YMap, adopt: CustomDomainProperty => Unit) {
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
              AmfArray(Seq(ScalarNode(entry.value).text()))
            case sequence: YSequence =>
              ArrayNode(sequence).text()
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
        val value = ScalarNode(entry.value)
        custom.set(CustomDomainPropertyModel.DisplayName, value.string(), Annotations(entry))
      })

      map.key("description", entry => {
        val value = ScalarNode(entry.value)
        custom.set(CustomDomainPropertyModel.Description, value.string(), Annotations(entry))
      })

      map.key(
        "schema",
        entry => {
          OasTypeParser(entry, shape => shape.adopted(custom.id))
            .parse()
            .foreach({ shape =>
              tracking(shape, custom.id)
              custom.set(CustomDomainPropertyModel.Schema, shape, Annotations(entry))
            })
        }
      )

      AnnotationParser(custom, map).parse()

      custom
    }
  }

  case class UserDocumentationParser(seq: Seq[YNode]) {
    def parse(): Seq[CreativeWork] =
      seq.map(n =>
        n.tagType match {
          case YType.Map => RamlCreativeWorkParser(n).parse()
          case YType.Str =>
            val text = n.as[YScalar].text
            ctx.declarations.findDocumentations(text, SearchScope.All) match {
              case Some(doc) => doc.link(text, Annotations(n)).asInstanceOf[CreativeWork]
              case _ =>
                val documentation = RamlCreativeWorkParser(YNode(YMap.empty)).parse()
                ctx.violation(DeclarationNotFound,
                              documentation.id,
                              s"not supported scalar $n.text for documentation item",
                              n)
                documentation
            }
      })
  }
}
