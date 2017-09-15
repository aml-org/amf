package amf.spec.oas

import amf.common.AMFToken.StringToken
import amf.common.core.Strings
import amf.common.{AMFAST, Lazy}
import amf.compiler.Root
import amf.document.Document
import amf.domain.Annotation.{
  DeclaredElement,
  DefaultPayload,
  EndPointBodyParameter,
  ExplicitField,
  SingleValueArray,
  _
}
import amf.domain._
import amf.domain.extensions.CustomDomainProperty
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.model.{AmfArray, AmfScalar}
import amf.shape.Shape
import amf.spec.Declarations
import amf.vocabulary.VocabularyMappings

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

/**
  * Oas 2.0 spec parser
  */
case class OasSpecParser(root: Root) {

  def parseDocument(): Document = {

    val entries = Entries(root.ast.last)

    val declarations = parseDeclares(entries)

    val api = parseWebApi(entries, Declarations(declarations)).add(SourceVendor(root.vendor))

    Document()
      .adopted(root.location)
      .withEncodes(api)
      .withDeclares(declarations)
  }

  private def parseDeclares(entries: Entries) =
    parseTypeDeclarations(entries, root.location + "#/declarations") ++
      parseAnnotationTypeDeclarations(entries, root.location + "#/declarations")

  def parseTypeDeclarations(entries: Entries, typesPrefix: String): Seq[Shape] = {
    val types = ListBuffer[Shape]()

    entries.key(
      "definitions",
      entry => {
        Entries(entry.value).entries.values.flatMap(entry => {
          val typeName = entry.key.content.unquote
          OasTypeParser(entry, shape => shape.withName(typeName).adopted(typesPrefix), Declarations(types))
            .parse() match {
            case Some(shape) =>
              types += shape.add(DeclaredElement())
            case None => throw new Exception(s"Error parsing shape at $typeName")
          }
        })
      }
    )
    types
  }

  def parseAnnotationTypeDeclarations(entries: Entries, customProperties: String): Seq[CustomDomainProperty] = {
    val customDomainProperties = ListBuffer[CustomDomainProperty]()

    entries.key(
      "x-annotationTypes",
      e => {
        Entries(e.value).entries.values.map(entry => {
          val typeName = entry.key.content.unquote
          val customProperty = AnnotationTypesParser(entry,
                                                     customProperty =>
                                                       customProperty
                                                         .withName(typeName)
                                                         .adopted(customProperties),
                                                     Declarations(customDomainProperties)).parse()
          customDomainProperties += customProperty.add(DeclaredElement())
        })
      }
    )

    customDomainProperties
  }

  private def parseWebApi(entries: Entries, declarations: Declarations): WebApi = {

    val api     = WebApi(root.ast).adopted(root.location)
    val entries = Entries(root.ast.last)

    entries.key(
      "info",
      entry => {
        val info = Entries(entry.value)

        info.key("title", entry => {
          val value = ValueNode(entry.value)
          api.set(WebApiModel.Name, value.string(), entry.annotations())
        })

        info.key("description", entry => {
          val value = ValueNode(entry.value)
          api.set(WebApiModel.Description, value.string(), entry.annotations())
        })

        info.key("termsOfService", entry => {
          val value = ValueNode(entry.value)
          api.set(WebApiModel.TermsOfService, value.string(), entry.annotations())
        })

        info.key("version", entry => {
          val value = ValueNode(entry.value)
          api.set(WebApiModel.Version, value.string(), entry.annotations())
        })

        info.key(
          "license",
          entry => {
            val license: License = LicenseParser(entry.value).parse()
            api.set(WebApiModel.License, license, entry.annotations())
          }
        )
      }
    )

    entries.key("host", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Host, value.string(), entry.annotations())
    })

    entries.key(
      "x-base-uri-parameters",
      entry => {
        val uriParameters = HeaderParametersParser(entry.value, api.withBaseUriParameter, declarations).parse()
        api.set(WebApiModel.BaseUriParameters, AmfArray(uriParameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key(
      "basePath",
      entry => {
        val value = ValueNode(entry.value)
        api.set(WebApiModel.BasePath, value.string(), entry.annotations())
      }
    )

    entries.key("consumes", entry => {
      val value = ArrayNode(entry.value)
      api.set(WebApiModel.Accepts, value.strings(), entry.annotations())
    })

    entries.key("produces", entry => {
      val value = ArrayNode(entry.value)
      api.set(WebApiModel.ContentType, value.strings(), entry.annotations())
    })

    entries.key("schemes", entry => {
      val value = ArrayNode(entry.value)
      api.set(WebApiModel.Schemes, value.strings(), entry.annotations())
    })

    entries.key(
      "contact",
      entry => {
        val organization: Organization = OrganizationParser(entry.value).parse()
        api.set(WebApiModel.Provider, organization, entry.annotations())
      }
    )

    entries.key(
      "externalDocs",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        api.set(WebApiModel.Documentation, creativeWork, entry.annotations())
      }
    )

    entries.key(
      "paths",
      entry => {
        val paths = Entries(entry.value)
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

    api
  }
}

case class EndpointParser(entry: EntryNode,
                          producer: String => EndPoint,
                          collector: mutable.ListBuffer[EndPoint],
                          declarations: Declarations) {

  def parse(): Unit = {

    val endpoint = producer(ValueNode(entry.key).string().value.toString).add(Annotations(entry.ast))
    val entries  = Entries(entry.value)

    entries.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), entry.annotations())
    })

    var parameters = OasParameters()

    entries.key(
      "parameters",
      entry => {
        parameters = ParametersParser(entry.value, endpoint.id, declarations).parse()
        parameters.body.foreach(_.add(EndPointBodyParameter()))
        parameters match {
          case OasParameters(_, path, _, _) if path.nonEmpty =>
            endpoint.set(EndPointModel.UriParameters, AmfArray(path, Annotations(entry.value)), entry.annotations())
          case _ =>
        }
      }
    )

    collector += endpoint

    entries.regex(
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

case class RequestParser(entries: Entries, global: OasParameters, producer: () => Request, declarations: Declarations) {
  def parse(): Option[Request] = {
    val request = new Lazy[Request](producer)

    var parameters = global

    entries.key(
      "parameters",
      entry => {
        parameters = global.merge(ParametersParser(entry.value, request.getOrCreate.id, declarations).parse())
        parameters match {
          case OasParameters(query, _, header, _) =>
            if (query.nonEmpty)
              request.getOrCreate.set(RequestModel.QueryParameters,
                                      AmfArray(query, Annotations(entry.value)),
                                      entry.annotations())
            if (header.nonEmpty)
              request.getOrCreate.set(RequestModel.Headers,
                                      AmfArray(header, Annotations(entry.value)),
                                      entry.annotations())
        }
      }
    )

    val payloads = mutable.ListBuffer[Payload]()
    parameters.body.foreach(payloads += _)

    entries.key(
      "x-request-payloads",
      entry =>
        ArrayNode(entry.value).values.map(value =>
          payloads += PayloadParser(value, request.getOrCreate.withPayload, declarations).parse())
    )

    if (payloads.nonEmpty) request.getOrCreate.set(RequestModel.Payloads, AmfArray(payloads))

    request.option
  }
}

case class OperationParser(entry: EntryNode,
                           global: OasParameters,
                           producer: String => Operation,
                           declarations: Declarations) {
  def parse(): Operation = {

    val operation = producer(ValueNode(entry.key).string().value.toString).add(Annotations(entry.ast))
    val entries   = Entries(entry.value)

    entries.key("operationId", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Name, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Description, value.string(), entry.annotations())
    })

    entries.key("deprecated", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Deprecated, value.boolean(), entry.annotations())
    })

    entries.key("summary", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Summary, value.string(), entry.annotations())
    })

    entries.key(
      "externalDocs",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        operation.set(OperationModel.Documentation, creativeWork, entry.annotations())
      }
    )

    entries.key(
      "schemes",
      entry => {
        val value = ArrayNode(entry.value)
        operation.set(OperationModel.Schemes, value.strings(), entry.annotations())
      }
    )

    RequestParser(entries, global, () => operation.withRequest(), declarations)
      .parse()
      .map(operation.set(OperationModel.Request, _))

    entries.key(
      "responses",
      entry => {
        Entries(entry.value).regex(
          "default|\\d{3}",
          entries => {
            val responses = mutable.ListBuffer[Response]()
            entries.foreach(entry => {
              responses += ResponseParser(entry, operation.withResponse, declarations).parse()
            })
            operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), entry.annotations())
          }
        )
      }
    )

    operation
  }
}

case class ParametersParser(ast: AMFAST, parentId: String, declarations: Declarations) {
  def parse(): OasParameters = {
    val parameters = ArrayNode(ast).values
      .map(value => ParameterParser(value, parentId, declarations).parse())

    OasParameters(
      parameters.filter(_.isQuery).map(_.parameter),
      parameters.filter(_.isPath).map(_.parameter),
      parameters.filter(_.isHeader).map(_.parameter),
      parameters.filter(_.isBody).map(_.payload).headOption
    )
  }
}

case class PayloadParser(payloadMap: AMFAST, producer: (Option[String]) => Payload, declarations: Declarations) {
  def parse(): Payload = {

    val entries = Entries(payloadMap)

    val payload = producer(
      entries.key("mediaType").map(entry => ValueNode(entry.value).string().value.toString)
    ).add(Annotations(payloadMap))

    // todo set again for not lose annotations?
    entries.key("mediaType",
                entry => payload.set(PayloadModel.MediaType, ValueNode(entry.value).string(), entry.annotations()))

    entries.key(
      "schema",
      entry => {
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(payload.id), declarations)
          .parse()
          .map(payload.set(PayloadModel.Schema, _, entry.annotations()))
      }
    )

    payload
  }
}

case class ResponseParser(entry: EntryNode, producer: String => Response, declarations: Declarations) {
  def parse(): Response = {

    val entries = Entries(entry.value)

    val node     = ValueNode(entry.key)
    val response = producer(node.string().value.toString).add(Annotations(entry.ast))

    if (response.name == "default") {
      response.set(ResponseModel.StatusCode, "200")
    } else {
      response.set(ResponseModel.StatusCode, node.string())
    }

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      response.set(ResponseModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] = HeaderParametersParser(entry.value, response.withHeader, declarations).parse()
        response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    val payloads = mutable.ListBuffer[Payload]()

    defaultPayload(entries, response.id).foreach(payloads += _)

    entries.key(
      "x-response-payloads",
      entry =>
        ArrayNode(entry.value).values.map(value =>
          payloads += PayloadParser(value, response.withPayload, declarations).parse())
    )

    if (payloads.nonEmpty)
      response.set(ResponseModel.Payloads, AmfArray(payloads))

    response
  }

  private def defaultPayload(entries: Entries, parentId: String): Option[Payload] = {
    val payload = Payload().add(DefaultPayload())

    entries.key("x-media-type",
                entry => payload.set(PayloadModel.MediaType, ValueNode(entry.value).string(), entry.annotations()))
    //TODO add parent id to payload?
    payload.adopted(parentId)

    entries.key(
      "schema",
      entry =>
        OasTypeParser(entry, (shape) => shape.withName("default").adopted(payload.id), declarations)
          .parse()
          .map(payload.set(PayloadModel.Schema, _, entry.annotations()))
    )

    if (payload.fields.nonEmpty) Some(payload) else None
  }
}

case class ParameterParser(ast: AMFAST, parentId: String, declarations: Declarations) {
  def parse(): OasParameter = {
    val p       = OasParameter(ast)
    val entries = Entries(ast)

    p.parameter.set(ParameterModel.Required, value = false)

    entries.key("name", entry => {
      val value = ValueNode(entry.value)
      p.parameter.set(ParameterModel.Name, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      p.parameter.set(ParameterModel.Description, value.string(), entry.annotations())
    })

    entries.key("required", entry => {
      val value = ValueNode(entry.value)
      p.parameter.set(ParameterModel.Required, value.boolean(), entry.annotations() += ExplicitField())
    })

    entries.key("in", entry => {
      val value = ValueNode(entry.value)
      p.parameter.set(ParameterModel.Binding, value.string(), entry.annotations())
    })

    //TODO generate parameter with parent id or adopt
    if (p.isBody) {
      p.payload.adopted(parentId)
      entries.key(
        "schema",
        entry => {
          OasTypeParser(entry, (shape) => shape.withName("schema").adopted(p.payload.id), declarations)
            .parse()
            .map(p.payload.set(PayloadModel.Schema, _, entry.annotations()))
        }
      )

      entries.key("x-media-type", entry => {
        val value = ValueNode(entry.value)
        p.payload.set(PayloadModel.MediaType, value.string(), entry.annotations())
      })

    } else {
      // type
      p.parameter.adopted(parentId)
      val map = MapNode(ast)
      OasTypeParser(map, shape => shape.withName("schema").adopted(p.parameter.id), declarations)
        .parse()
        .map(p.parameter.set(ParameterModel.Schema, _, map.annotations()))
    }

    p
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

  private def merge(global: Option[Payload], inner: Option[Payload]): Option[Payload] =
    inner.map(_.add(DefaultPayload())).orElse(global.map(_.copy()))

  private def merge(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
    val globalMap = global.map(p => p.name -> p.copy().add(Annotation.EndPointParameter())).toMap
    val innerMap  = inner.map(p => p.name  -> p.copy()).toMap

    (globalMap ++ innerMap).values.toSeq
  }
}

case class OasParameter(ast: AMFAST) {
  val parameter = Parameter(ast)
  val payload   = Payload(ast)

  def isBody: Boolean   = parameter.isBody
  def isQuery: Boolean  = parameter.isQuery
  def isPath: Boolean   = parameter.isPath
  def isHeader: Boolean = parameter.isHeader
}

case class LicenseParser(ast: AMFAST) {
  def parse(): License = {
    val license = License(ast)
    val entries = Entries(ast)

    entries.key("url", entry => {
      val value = ValueNode(entry.value)
      license.set(LicenseModel.Url, value.string(), entry.annotations())
    })

    entries.key("name", entry => {
      val value = ValueNode(entry.value)
      license.set(LicenseModel.Name, value.string(), entry.annotations())
    })

    license
  }
}

case class HeaderParametersParser(ast: AMFAST, producer: String => Parameter, declarations: Declarations) {
  def parse(): Seq[Parameter] = {
    Entries(ast).entries.values
      .map(entry => HeaderParameterParser(entry, producer, declarations).parse())
      .toSeq
  }
}

case class HeaderParameterParser(entry: EntryNode, producer: String => Parameter, declarations: Declarations) {
  def parse(): Parameter = {

    val name      = entry.key.content.unquote
    val parameter = producer(name).add(Annotations(entry.ast))

    parameter
      .set(ParameterModel.Required, !name.endsWith("?"))
      .set(ParameterModel.Name, ValueNode(entry.key).string())

    val entries = Entries(entry.value)

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Description, value.string(), entry.annotations())
    })

    entries.key("required", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Required, value.boolean(), entry.annotations() += ExplicitField())
    })

    entries.key(
      "type",
      _ => {
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.id), declarations)
          .parse()
          .map(parameter.set(ParameterModel.Schema, _, entry.annotations()))
      }
    )

    parameter
  }
}

case class CreativeWorkParser(ast: AMFAST) {
  def parse(): CreativeWork = {
    val creativeWork = CreativeWork(ast)
    val entries      = Entries(ast)

    entries.key("url", entry => {
      val value = ValueNode(entry.value)
      creativeWork.set(CreativeWorkModel.Url, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      creativeWork.set(CreativeWorkModel.Description, value.string(), entry.annotations())
    })

    creativeWork
  }
}

case class OrganizationParser(ast: AMFAST) {
  def parse(): Organization = {

    val organization = Organization(ast)
    val entries      = Entries(ast)

    entries.key("url", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Url, value.string(), entry.annotations())
    })

    entries.key("name", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Name, value.string(), entry.annotations())
    })

    entries.key("email", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Email, value.string(), entry.annotations())
    })

    organization
  }
}

case class AnnotationTypesParser(node: EntryNode, adopt: (CustomDomainProperty) => Unit, declarations: Declarations) {
  def parse(): CustomDomainProperty = {
    val custom         = CustomDomainProperty(node.annotations())
    val annotationName = node.key.content.unquote
    custom.withName(annotationName)
    adopt(custom)

    val entries = Entries(node.value)

    entries.key(
      "allowedTargets",
      entry => {
        val annotations = entry.annotations()
        val targets: AmfArray = entry.value.`type` match {
          case StringToken =>
            annotations += SingleValueArray()
            AmfArray(Seq(ValueNode(entry.value).string()))
          case _ =>
            ArrayNode(entry.value).strings()
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

    entries.key("displayName", entry => {
      val value = ValueNode(entry.value)
      custom.set(CustomDomainPropertyModel.DisplayName, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      custom.set(CustomDomainPropertyModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => shape.adopted(custom.id), declarations)
          .parse()
          .foreach({ shape =>
            custom.set(CustomDomainPropertyModel.Schema, shape, entry.annotations())
          })
      }
    )

    custom
  }
}

case class Entries(ast: AMFAST) {
  def key(keyword: String): Option[EntryNode] = entries.get(keyword)

  def key(keyword: String, fn: (EntryNode => Unit)): Unit = key(keyword).foreach(fn)

  def regex(regex: String, fn: (Iterable[EntryNode] => Unit)): Unit = {
    val path: Regex = regex.r
    val values = entries
      .filterKeys({
        case path() => true
        case _      => false
      })
      .values
    if (values.nonEmpty) fn(values)
  }

  var entries: ListMap[String, EntryNode] = ListMap(ast.children.map(n => n.head.content.unquote -> EntryNode(n)): _*)

}

trait KeyValueNode {
  val key: AMFAST
  val value: AMFAST
  val ast: AMFAST

  def annotations(): Annotations
}

case class EntryNode(ast: AMFAST) extends KeyValueNode {

  override val key: AMFAST   = ast.head
  override val value: AMFAST = Option(ast).filter(_.children.size > 1).map(_.last).orNull

  override def annotations(): Annotations = Annotations(ast)
}

case class MapNode(ast: AMFAST) extends KeyValueNode {

  override val key: AMFAST   = AMFAST.EMPTY_NODE
  override val value: AMFAST = ast

  override def annotations(): Annotations = Annotations(ast)
}

case class ArrayNode(ast: AMFAST) {

  def strings(): AmfArray = {
    val elements = ast.children.map(child => ValueNode(child).string())
    AmfArray(elements, annotations())
  }

  val values: Seq[AMFAST] = ast.children

  private def annotations() = Annotations(ast)
}

case class ValueNode(ast: AMFAST) {

  def string(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content, annotations())
  }

  def boolean(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toBoolean, annotations())
  }

  def integer(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toInt, annotations())
  }

  def negated(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(!content.toBoolean, annotations())
  }

  private def annotations() = Annotations(ast)
}
