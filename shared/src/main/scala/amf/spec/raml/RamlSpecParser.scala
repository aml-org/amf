package amf.spec.raml

import amf.common.AMFToken.StringToken
import amf.common.Strings.strings
import amf.common.{AMFAST, Lazy}
import amf.compiler.Root
import amf.domain.Annotation.{ExplicitField, ParentEndPoint, SingleValueArray, SynthesizedField}
import amf.domain._
import amf.maker.BaseUriSplitter
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain.OperationModel.Method
import amf.metadata.domain._
import amf.model.{AmfArray, AmfElement, AmfScalar}

import scala.collection.mutable
import scala.util.matching.Regex

/**
  * Raml 1.0 spec parser
  */
case class RamlSpecParser(root: Root) {

  def parseWebApi(): WebApi = {

    val api     = WebApi(root.ast).adopted(root.location)
    val entries = Entries(root.ast.last)

    entries.key("title", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Name, value.string(), entry.annotations())
    })

    entries.key(
      "baseUriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value, api.withBaseUriParameter).parse().map(_.withBinding("path"))
        api.set(WebApiModel.BaseUriParameters, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "mediaType",
      entry => {
        val annotations = entry.annotations()
        val value: AmfElement = entry.value.`type` match {
          case StringToken =>
            annotations += SingleValueArray()
            AmfArray(Seq(ValueNode(entry.value).string()))
          case _ =>
            ArrayNode(entry.value).strings()
        }

        api.set(WebApiModel.ContentType, value, annotations)
        api.set(WebApiModel.Accepts, value, annotations)
      }
    )

    entries.key("version", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Version, value.string(), entry.annotations())
    })

    entries.key("(termsOfService)", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.TermsOfService, value.string(), entry.annotations())
    })

    entries.key("protocols", entry => {
      val value = ArrayNode(entry.value)
      api.set(WebApiModel.Schemes, value.strings(), entry.annotations())
    })

    entries.key(
      "(contact)",
      entry => {
        val organization: Organization = OrganizationParser(entry.value).parse()
        api.set(WebApiModel.Provider, organization, entry.annotations())
      }
    )

    entries.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        api.set(WebApiModel.Documentation, creativeWork, entry.annotations())
      }
    )

    entries.key("(license)", entry => {
      val license: License = LicenseParser(entry.value).parse()
      api.set(WebApiModel.License, license, entry.annotations())
    })

    entries.regex(
      "^/.*",
      entries => {
        val endpoints = mutable.ListBuffer[EndPoint]()
        entries.foreach(entry => EndpointParser(entry, api.withEndPoint, None, endpoints).parse())
        api.set(WebApiModel.EndPoints, AmfArray(endpoints))
      }
    )

    entries.key(
      "baseUri",
      entry => {
        val value = ValueNode(entry.value)
        val uri   = BaseUriSplitter(value.string().value.toString)

        if (api.schemes.isEmpty && uri.protocol.nonEmpty) {
          api.set(WebApiModel.Schemes,
                  AmfArray(Seq(AmfScalar(uri.protocol)), Annotations(entry.value) += SynthesizedField()),
                  entry.annotations())
        }

        if (uri.domain.nonEmpty) {
          api.set(WebApiModel.Host,
                  AmfScalar(uri.domain, Annotations(entry.value) += SynthesizedField()),
                  entry.annotations())
        }

        if (uri.path.nonEmpty) {
          api.set(WebApiModel.BasePath,
                  AmfScalar(uri.path, Annotations(entry.value) += SynthesizedField()),
                  entry.annotations())
        }
      }
    )

    entries.key(
      "types",
      entry => {
        val types = RamlTypesParser(entry.value, shape => shape.adopted(api.id)).parse()
        println(types)
      }
    )

    api
  }
}

case class EndpointParser(entry: EntryNode,
                          producer: String => EndPoint,
                          parent: Option[EndPoint],
                          collector: mutable.ListBuffer[EndPoint]) {
  def parse(): Unit = {

    val path = parent.map(_.path).getOrElse("") + entry.key.content.unquote

    val endpoint = producer(path).add(Annotations(entry.ast))
    parent.map(p => endpoint.add(ParentEndPoint(p)))
    val entries = Entries(entry.value)

    endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

    entries.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "uriParameters",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value, endpoint.withParameter).parse().map(_.withBinding("path"))
        endpoint.set(EndPointModel.UriParameters, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.regex(
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

    entries.regex(
      "^/.*",
      entries => {
        entries.foreach(EndpointParser(_, producer, Some(endpoint), collector).parse())
      }
    )
  }
}

case class RequestParser(entries: Entries, producer: () => Request) {

  def parse(): Option[Request] = {
    val request = new Lazy[Request](producer)
    entries.key(
      "queryParameters",
      entry => {

        val parameters: Seq[Parameter] =
          ParametersParser(entry.value, request.getOrCreate.withQueryParameter).parse().map(_.withBinding("query"))
        request.getOrCreate.set(RequestModel.QueryParameters,
                                AmfArray(parameters, Annotations(entry.value)),
                                entry.annotations())
      }
    )

    entries.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] =
          ParametersParser(entry.value, request.getOrCreate.withHeader).parse().map(_.withBinding("header"))
        request.getOrCreate.set(RequestModel.Headers,
                                AmfArray(parameters, Annotations(entry.value)),
                                entry.annotations())
      }
    )

    entries.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        RamlTypeParser(entry, shape => shape.withName("default").adopted(request.getOrCreate.id))
          .parse()
          .foreach(payloads += request.getOrCreate.withPayload(None).withSchema(_)) //todo

        Entries(entry.value)
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
            .set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), entry.annotations())
      }
    )

    request.option
  }
}

case class OperationParser(entry: EntryNode, producer: (String) => Operation) {

  def parse(): Operation = {

    val method = entry.key.content.unquote

    val operation = producer(method).add(Annotations(entry.ast))

    val entries = Entries(entry.value)

    operation.set(Method, ValueNode(entry.key).string())

    entries.key("displayName", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Name, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Description, value.string(), entry.annotations())
    })

    entries.key("(deprecated)", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Deprecated, value.boolean(), entry.annotations())
    })

    entries.key("(summary)", entry => {
      val value = ValueNode(entry.value)
      operation.set(OperationModel.Summary, value.string(), entry.annotations())
    })

    entries.key(
      "(externalDocs)",
      entry => {
        val creativeWork: CreativeWork = CreativeWorkParser(entry.value).parse()
        operation.set(OperationModel.Documentation, creativeWork, entry.annotations())
      }
    )

    entries.key(
      "protocols",
      entry => {
        val value = ArrayNode(entry.value)
        operation.set(OperationModel.Schemes, value.strings(), entry.annotations())
      }
    )

    RequestParser(entries, () => operation.withRequest()).parse().map(operation.set(OperationModel.Request, _))

    entries.key(
      "responses",
      entry => {
        Entries(entry.value).regex(
          "\\d{3}",
          entries => {
            val responses = mutable.ListBuffer[Response]()
            entries.foreach(entry => { responses += ResponseParser(entry, operation.withResponse).parse() })
            operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), entry.annotations())
          }
        )
      }
    )

    operation
  }
}

case class ParametersParser(ast: AMFAST, producer: String => Parameter) {
  def parse(): Seq[Parameter] = {
    Entries(ast).entries.values
      .map(entry => ParameterParser(entry, producer).parse())
      .toSeq
  }
}

case class PayloadParser(entry: EntryNode, producer: (Option[String]) => Payload) {
  def parse(): Payload = {

    val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry.ast))

    Option(entry.value).foreach(
      _ =>
        RamlTypeParser(entry, shape => shape.withName("schema").adopted(payload.id))
          .parse()
          .foreach(payload.withSchema))
    payload
  }
}

case class ResponseParser(entry: EntryNode, producer: (String) => Response) {
  def parse(): Response = {

    val node = ValueNode(entry.key)

    val response = producer(node.string().value.toString).add(Annotations(entry.ast))
    val entries  = Entries(entry.value)

    response.set(ResponseModel.StatusCode, node.string())

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      response.set(ResponseModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] = ParametersParser(entry.value, response.withHeader).parse()
        response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key(
      "body",
      entry => {
        val payloads = mutable.ListBuffer[Payload]()

        val payload = Payload()
        payload.adopted(response.id) // TODO review

        RamlTypeParser(entry, shape => shape.withName("default").adopted(payload.id))
          .parse()
          .foreach(payloads += payload.withSchema(_))

        Entries(entry.value).regex(
          ".*/.*",
          entries => {
            entries.foreach(entry => { payloads += PayloadParser(entry, response.withPayload).parse() })
          }
        )
        if (payloads.nonEmpty)
          response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), entry.annotations())
      }
    )

    response
  }
}

case class ParameterParser(entry: EntryNode, producer: String => Parameter) {
  def parse(): Parameter = {

    val name      = entry.key.content.unquote
    val parameter = producer(name).add(Annotations(entry.ast))

    parameter.set(ParameterModel.Required, value = true)

    if (name.endsWith("?")) {
      parameter.set(ParameterModel.Name, name.stripSuffix("?"))
      parameter.set(ParameterModel.Required, value = false)
    } else {
      parameter.set(ParameterModel.Name, name)
    }

    val entries = Entries(entry.value)

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Description, value.string(), entry.annotations())
    })

    entries.key("required", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Required, value.boolean(), entry.annotations() += ExplicitField())
    })

    RamlTypeParser(entry, shape => shape.withName("schema").adopted(parameter.id))
      .parse()
      .foreach(parameter.set(ParameterModel.Schema, _, entry.annotations()))

    parameter
  }
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

  var entries: Map[String, EntryNode] = ast.children.map(n => n.head.content.unquote -> EntryNode(n)).toMap

}

case class EntryNode(ast: AMFAST) {

  val key: AMFAST   = ast.head
  val value: AMFAST = Option(ast).filter(_.children.size > 1).map(_.last).orNull

  def annotations(): Annotations = Annotations(ast)
}

case class ArrayNode(ast: AMFAST) {

  def strings(): AmfArray = {
    val elements = ast.children.map(child => ValueNode(child).string())
    AmfArray(elements, annotations())
  }

  private def annotations() = Annotations(ast)
}

case class ValueNode(ast: AMFAST) {

  def string(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content, annotations())
  }

  def integer(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toInt, annotations())
  }

  def boolean(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toBoolean, annotations())
  }

  def negated(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(!content.toBoolean, annotations())
  }

  private def annotations() = Annotations(ast)
}
