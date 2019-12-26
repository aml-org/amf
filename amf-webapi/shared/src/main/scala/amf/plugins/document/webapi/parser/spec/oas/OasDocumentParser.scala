package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.annotations._
import amf.core.metamodel.Field
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{AmfStrings, IdCounter, Lazy, TemplateUri}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.spec
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.{ErrorCallback, ErrorRequest}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isOasAnnotation
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, WebApiBaseSpecParser}
import amf.plugins.document.webapi.parser.spec.declaration.{AbstractDeclarationsParser, SecuritySchemeParser, _}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.shapes.models.{CreativeWork, NodeShape}
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.metamodel.{EndPointModel, _}
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations._
import amf.plugins.features.validation.CoreValidations.DeclarationNotFound
import org.yaml.model.{YMapEntry, YNode, _}

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
            val idCounter = new IdCounter()
            val securedBy =
              entry.value
                .as[Seq[YNode]]
                .map(s => OasSecurityRequirementParser(s, api.withSecurity, idCounter).parse()) // todo when generating id for security requirements webapi id is null
                .collect { case Some(s) => s }
            api.set(WebApiModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
          case _ =>
            ctx.violation(InvalidSecurityRequirementsSeq,
                          entry.value,
                          "'security' must be an array of security requirement object")
        }
      }
    )

    val documentations = ListBuffer[CreativeWork]()

    map.key(
      "externalDocs",
      entry => {
        documentations += OasCreativeWorkParser(entry.value, api.id).parse()
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

  case class EndpointParser(entry: YMapEntry, producer: String => EndPoint, collector: mutable.ListBuffer[EndPoint]) {

    def parse(): Unit = {
      val path = entry.key.as[YScalar].text

      val endpoint = producer(path).add(Annotations(entry))

      checkBalancedParams(path, entry.value, endpoint.id, EndPointModel.Path.value.iri(), ctx)
      endpoint.set(EndPointModel.Path, AmfScalar(path, Annotations(entry.key)))

      if (!TemplateUri.isValid(path))
        ctx.violation(InvalidEndpointPath, endpoint.id, TemplateUri.invalidMsg(path), entry.value)

      if (collector.exists(other => other.path.option() exists (identicalPaths(_, path))))
        ctx.violation(DuplicatedEndpointPath, endpoint.id, "Duplicated resource path " + path, entry)
      else parseEndpoint(endpoint)
    }

    /**
      * Verify if two paths are identical. In the case of OAS 3.0, paths with the same hierarchy but different templated
      * names are considered identical.
      */
    private def identicalPaths(first: String, second: String): Boolean = {
      def stripPathParams(s: String): String = {
        val trimmed = if (s.endsWith("/")) s.init else s
        trimmed.replaceAll("\\{.*?\\}", "")
      }
      if (ctx.syntax == Oas3Syntax) stripPathParams(first) == stripPathParams(second)
      else first == second
    }

    private def parseEndpoint(endpoint: EndPoint) =
      ctx.link(entry.value) match {
        case Left(value) =>
          ctx.obtainRemoteYNode(value).orElse(ctx.declarations.asts.get(value)) match {
            case Some(map) if map.tagType == YType.Map =>
              parseEndpointMap(endpoint, map.as[YMap])
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
      if (ctx.syntax == Oas3Syntax) {
        map.key("summary", EndPointModel.Summary in endpoint)
        map.key("description", EndPointModel.Description in endpoint)
      }
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
          val idCounter = new IdCounter()
          val securedBy = entry.value
            .as[Seq[YNode]]
            .map(s => OasSecurityRequirementParser(s, endpoint.withSecurity, idCounter).parse())
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
            operations += operationParser(entry, endpoint.withOperation).parse()
          }
          endpoint.set(EndPointModel.Operations, AmfArray(operations))
        }
      )
    }
  }

  case class RequestParser(map: YMap, parentId: String, definitionEntry: YMapEntry)(implicit ctx: OasWebApiContext) {
    def parse(): Option[Request] = {
      if (ctx.syntax == Oas3Syntax) {
        Some(Oas3RequestParser(map, parentId, definitionEntry).parse())
      } else {
        Oas2RequestParser(map, (r: Request) => r.adopted(parentId)).parse()
      }
    }
  }

  case class Oas3RequestParser(map: YMap, parentId: String, definitionEntry: YMapEntry)(implicit ctx: OasWebApiContext) {

    private def adopt(request: Request) = {
      request
        .add(Annotations(definitionEntry))
        .set(RequestModel.Name, ScalarNode(definitionEntry.key).string())
        .adopted(parentId)
    }

    def parse(): Request = {
      ctx.link(map) match {
        case Left(fullRef) =>
          parseRef(fullRef)
        case Right(_) =>
          val request = adopt(Request())

          map.key("description", RequestModel.Description in request)
          map.key("required", RequestModel.Required in request)

          val payloads = mutable.ListBuffer[Payload]()

          map.key("content") match {
            case Some(entry) =>
              payloads ++= OasContentsParser(entry, request.withPayload).parse()
            case None =>
              ctx.violation(RequestBodyContentRequired,
                            request.id,
                            s"Request body must have a 'content' field defined",
                            map)
          }
          request.set(ResponseModel.Payloads, AmfArray(payloads))

          AnnotationParser(request, map).parse()
          ctx.closedShape(request.id, map, "request")
          request
      }
    }

    private def parseRef(fullRef: String): Request = {
      val name = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "requestBodies")
      ctx.declarations
        .findRequestBody(name, SearchScope.Named)
        .map(req => adopt(req.link(name, Annotations(map))))
        .getOrElse {
          ctx.obtainRemoteYNode(fullRef) match {
            case Some(requestNode) =>
              Oas3RequestParser(requestNode.as[YMap], parentId, definitionEntry).parse()
            case None =>
              ctx.violation(CoreValidations.UnresolvedReference,
                            "",
                            s"Cannot find requestBody reference $fullRef",
                            map)
              adopt(ErrorRequest(fullRef, map).link(name))
          }
        }
    }
  }

  case class Oas2RequestParser(map: YMap, adopt: Request => Unit)(implicit ctx: OasWebApiContext) {
    def parse(): Option[Request] = {
      val request = new Lazy[Request](() => {
        val req = Request().add(VirtualObject())
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

  /**
    * A single named callback may be parsed into multiple Callback when multiple expressions are defined.
    * This is due to inconsistency in the model, pending refactor in APIMF-1771
    */
  case class CallbackParser(map: YMap, adopt: Callback => Unit, name: String, rootEntry: YMapEntry) {
    def parse(): List[Callback] = {
      ctx.link(map) match {
        case Left(fullRef) =>
          val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "callbacks")
          ctx.declarations
            .findCallbackInDeclarations(label)
            .map { callbacks =>
              callbacks.map { callback =>
                val linkCallback: Callback = callback.link(label, Annotations(map))
                adopt(linkCallback)
                linkCallback
              }
            }
            .getOrElse {
              ctx.obtainRemoteYNode(fullRef) match {
                case Some(callbackNode) =>
                  CallbackParser(callbackNode.as[YMap], adopt, name, rootEntry).parse()
                case None =>
                  ctx.violation(CoreValidations.UnresolvedReference,
                                "",
                                s"Cannot find callback reference $fullRef",
                                map)
                  val callback: Callback = new ErrorCallback(label, map).link(name, Annotations(rootEntry))

                  adopt(callback)
                  List(callback)
              }
            }
        case Right(_) =>
          val callbackEntries = map.entries
          callbackEntries.map { entry =>
            val expression = entry.key.as[YScalar].text
            val callback   = Callback().add(Annotations(entry))
            callback.fields.setWithoutId(CallbackModel.Expression, AmfScalar(expression, Annotations(entry.key)))
            adopt(callback)
            val collector = mutable.ListBuffer[EndPoint]()
            EndpointParser(entry, callback.withEndpoint, collector).parse()
            collector.foreach(_.withPath(s"/$expression")) // rename path to avoid endpoint validations
            callback
          }.toList

      }

    }
  }

  case class Oas3OperationParser(entry: YMapEntry, producer: String => Operation)
      extends OperationParser(entry, producer) {
    override protected def parseVersionFacets(operation: Operation, map: YMap): Unit = {
      map.key(
        "requestBody",
        entry => {
          operation.withRequest(Oas3RequestParser(entry.value.as[YMap], operation.id, entry).parse())
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
            .flatMap { callbackEntry =>
              val name = callbackEntry.key.as[YScalar].text
              CallbackParser(callbackEntry.value.as[YMap], _.withName(name).adopted(operation.id), name, callbackEntry)
                .parse()
            }
          operation.withCallbacks(callbacks)
        }
      )

      if (operation.fields.exists(OperationModel.Request)) operation.request.annotations += VirtualObject()
      ctx.factory.serversParser(map, operation).parse()
    }
  }

  case class Oas2OperationParser(entry: YMapEntry, producer: String => Operation)
      extends OperationParser(entry, producer) {
    override protected def parseVersionFacets(operation: Operation, map: YMap): Unit = {
      RequestParser(map, operation.id, entry)
        .parse()
        .map(r => operation.set(OperationModel.Request, AmfArray(Seq(r)), Annotations() += SynthesizedField()))

      map.key("schemes", OperationModel.Schemes in operation)
      map.key("consumes", OperationModel.Accepts in operation)
      map.key("produces", OperationModel.ContentType in operation)
    }
  }

  // move to oas factory?? cannot access inners parsers from outside
  private def operationParser: (YMapEntry, String => Operation) => OperationParser = {
    ctx.syntax match {
      case Oas3Syntax => Oas3OperationParser.apply
      case _          => Oas2OperationParser.apply // default?
    }
  }

  abstract class OperationParser(entry: YMapEntry, producer: String => Operation) {

    protected def parseVersionFacets(operation: Operation, map: YMap)

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
      map.key("externalDocs",
              OperationModel.Documentation in operation using (OasCreativeWorkParser.parse(_, operation.id)))

      // oas 3.0.0 / oas 2.0
      map.key(
        "tags",
        entry => {
          val tags = StringTagsParser(entry.value.as[YSequence], operation.id).parse()
          operation.set(OperationModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
        }
      )

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
          val idCounter = new IdCounter()
          // TODO check for empty array for resolution ?
          val securedBy = entry.value
            .as[Seq[YNode]]
            .map(s => OasSecurityRequirementParser(s, operation.withSecurity, idCounter).parse())
            .collect { case Some(s) => s }

          operation.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
        }
      )

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

      parseVersionFacets(operation, map)
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
