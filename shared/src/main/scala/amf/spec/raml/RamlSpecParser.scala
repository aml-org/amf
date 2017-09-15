package amf.spec.raml

import amf.common.Lazy
import amf.common.core.Strings
import amf.compiler.Root
import amf.document.Document
import amf.domain.Annotation._
import amf.domain._
import amf.maker.BaseUriSplitter
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain.OperationModel.Method
import amf.metadata.domain._
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.Shape
import org.yaml.model._

import amf.spec.BaseSpecParser._

import scala.collection.mutable

/**
  * Raml 1.0 spec parser
  */
case class RamlSpecParser(root: Root) {

  def parseDocument(): Document = {

    val document = Document().adopted(root.location)

    root.document.value.foreach(value => {
      val map = value.toMap

      val declarations = parseDeclares(map)

      val api = parseWebApi(map, declarations)

      document.withEncodes(api).withDeclares(declarations.values.toSeq)
    })

    document
  }

  private def parseDeclares(map: YMap) = {
    val definitions = root.location + "#/definitions"

    var declarations: Map[String, Shape] = Map()

    map.key(
      "types",
      entry => {
        val types = RamlTypesParser(entry.value.value.toMap, shape => { shape.adopted(definitions) }).parse()
        types.foreach(shape => {
          declarations += shape.name -> shape.add(DeclaredElement())
        })
      }
    )

    declarations
  }

  private def parseWebApi(map: YMap, declarations: Map[String, Shape]): WebApi = {

    val api = WebApi(map).adopted(root.location)

    map.key("title", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Name, value.string(), Annotations(entry))
    })

    map.key(
      "baseUriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value.value.toMap, api.withBaseUriParameter).parse().map(_.withBinding("path"))
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

    map.key("protocols", entry => {
      val value = ArrayNode(entry.value.value.toSequence)
      api.set(WebApiModel.Schemes, value.strings(), Annotations(entry))
    })

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

    map.key("(license)", entry => {
      val license: License = LicenseParser(entry.value.value.toMap).parse()
      api.set(WebApiModel.License, license, Annotations(entry))
    })

    map.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry => EndpointParser(entry, api.withEndPoint, None, endpoints).parse())
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

    api
  }
}

case class EndpointParser(entry: YMapEntry,
                          producer: String => EndPoint,
                          parent: Option[EndPoint],
                          collector: mutable.ListBuffer[EndPoint]) {
  def parse(): Unit = {

    val path = parent.map(_.path).getOrElse("") + entry.key.value.toScalar.text.unquote

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
          ParametersParser(entry.value.value.toMap, endpoint.withParameter).parse().map(_.withBinding("path"))
        endpoint.set(EndPointModel.UriParameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.regex(
      "get|patch|put|post|delete|options|head",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {
          operations += OperationParser(entry, endpoint.withOperation).parse()
        })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    collector += endpoint

    map.regex(
      "^/.*",
      entries => {
        entries.foreach(EndpointParser(_, producer, Some(endpoint), collector).parse())
      }
    )
  }
}

case class RequestParser(map: YMap, producer: () => Request) {

  def parse(): Option[Request] = {
    val request = new Lazy[Request](producer)
    map.key(
      "queryParameters",
      entry => {

        val parameters: Seq[Parameter] =
          ParametersParser(entry.value.value.toMap, request.getOrCreate.withQueryParameter)
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
          ParametersParser(entry.value.value.toMap, request.getOrCreate.withHeader)
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

        RamlTypeParser(entry, shape => shape.withName("default").adopted(request.getOrCreate.id))
          .parse()
          .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) //todo

        entry.value.value.toMap
          .regex(
            ".*/.*",
            entries => {
              entries.foreach(entry => {
                payloads += PayloadParser(entry, producer = request.getOrCreate.withPayload).parse()
              })
            }
          )
        if (payloads.nonEmpty)
          request.getOrCreate
            .set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )

    request.option
  }
}

case class OperationParser(entry: YMapEntry, producer: (String) => Operation) {

  def parse(): Operation = {

    val method = entry.key.value.toScalar.text.unquote

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

    RequestParser(map, () => operation.withRequest()).parse().map(operation.set(OperationModel.Request, _))

    map.key(
      "responses",
      entry => {
        entry.value.value.toMap.regex(
          "\\d{3}",
          entries => {
            val responses = mutable.ListBuffer[Response]()
            entries.foreach(entry => { responses += ResponseParser(entry, operation.withResponse).parse() })
            operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), Annotations(entry))
          }
        )
      }
    )

    operation
  }
}

case class ParametersParser(map: YMap, producer: String => Parameter) {
  def parse(): Seq[Parameter] =
    map.entries
      .map(entry => ParameterParser(entry, producer).parse())
}

case class PayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload) {
  def parse(): Payload = {

    val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry))

    Option(entry.value).foreach(
      _ =>
        RamlTypeParser(entry, shape => shape.withName("schema").adopted(payload.id))
          .parse()
          .foreach(payload.withSchema))
    payload
  }
}

case class ResponseParser(entry: YMapEntry, producer: (String) => Response) {
  def parse(): Response = {

    val node = ValueNode(entry.key)

    val response = producer(node.string().value.toString).add(Annotations(entry))
    val map      = entry.value.value.toMap

    response.set(ResponseModel.StatusCode, node.string())

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      response.set(ResponseModel.Description, value.string(), Annotations(entry))
    })

    map.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] = ParametersParser(entry.value.value.toMap, response.withHeader).parse()
        response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        val payload = Payload()
        payload.adopted(response.id) // TODO review

        RamlTypeParser(entry, shape => shape.withName("default").adopted(payload.id))
          .parse()
          .foreach(payloads += payload.withSchema(_))

        entry.value.value.toMap.regex(
          ".*/.*",
          entries => {
            entries.foreach(entry => { payloads += PayloadParser(entry, response.withPayload).parse() })
          }
        )
        if (payloads.nonEmpty)
          response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
      }
    )

    response
  }
}

case class ParameterParser(entry: YMapEntry, producer: String => Parameter) {
  def parse(): Parameter = {

    val name      = entry.key.value.toScalar.text.unquote
    val parameter = producer(name).add(Annotations(entry)) // TODO parameter id is using a name that is not final.
    val map       = entry.value.value.toMap

    map.key("required", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Required, value.boolean(), Annotations(entry) += ExplicitField())
    })

    if (parameter.fields.entry(ParameterModel.Required).isEmpty) {
      val required = !name.endsWith("?")

      parameter.set(ParameterModel.Required, required)
      parameter.set(ParameterModel.Name, if (required) name else name.stripSuffix("?"))
    }

    map.key("description", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Description, value.string(), Annotations(entry))
    })

    RamlTypeParser(entry, shape => shape.withName("schema").adopted(parameter.id))
      .parse()
      .foreach(parameter.set(ParameterModel.Schema, _, Annotations(entry)))

    parameter
  }
}