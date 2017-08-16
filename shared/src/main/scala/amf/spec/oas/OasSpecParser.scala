package amf.spec.oas

import amf.common.AMFAST
import amf.common.Strings.strings
import amf.compiler.Root
import amf.domain.Annotation.ExplicitField
import amf.domain._
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain.OperationModel.Method
import amf.metadata.domain._
import amf.model.{AmfArray, AmfScalar}
import amf.spec.{EntryNode, _}

import scala.collection.mutable
import scala.util.matching.Regex

/**
  * Oas 2.0 spec parser
  */
case class OasSpecParser(root: Root) {

  def parseWebApi(): WebApi = {

    val api     = WebApi(root.ast)
    val entries = new Entries(root.ast)

    entries.key("info", entry => {
        //TODO lexical for 'info' node is lost.
      val info = new Entries(entry.ast)
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
        "(license)",
        entry => {
          val license: License = LicenseParser(entry.value).parse()
          api.set(WebApiModel.License, license, entry.annotations())
        }
      )
    })

    entries.key("host", entry => {
      val value = ValueNode(entry.value)
      api.set(WebApiModel.Host, value.string(), entry.annotations())
    })

    entries.key(
      "x-baseUriParameters",
      entry => {
        //TODO Params
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

    entries.key("paths", entry => {
      val paths = new Entries(entry.ast)
      paths.regex(
        "^/.*",
        entries => {
          val endpoints = mutable.ListBuffer[EndPoint]()
          entries.foreach(EndpointParser(_, endpoints).parse())
          api.set(WebApiModel.EndPoints, AmfArray(endpoints))
        }
      )
    })

    api
  }
}

case class EndpointParser(entry: EntryNode, collector: mutable.ListBuffer[EndPoint]) {
  def parse(): Unit = {

    val endpoint = EndPoint(entry.ast)
    val entries  = new Entries(entry.value)

    endpoint.set(Path, ValueNode(entry.key).string())

    entries.key("displayName", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Name, value.string(), entry.annotations())
    })

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      endpoint.set(EndPointModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      //TODO parameters and 'body' payload logic :S
      "parameters",
      entry => {
        val parameters: Seq[Parameter] = ParametersParser(entry.value).parse()
        endpoint.set(EndPointModel.Parameters,
                     AmfArray(parameters, Annotations(entry.value)),
                     entry.annotations())
      }
    )

    entries.regex(
      "get|patch|put|post|delete|options|head",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => { operations += OperationParser(entry).parse() })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )
  }
}

case class RequestParser(entries: Entries) {

  def parse(): Request = {
    val request = Request()

    entries.key(
      "queryParameters",
      entry => {
        val parameters: Seq[Parameter] = ParametersParser(entry.value).parse()
        request.set(RequestModel.QueryParameters, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] = ParametersParser(entry.value).parse()
        request.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key(
      "body",
      entry => {
        new Entries(entry.value).regex(
          ".*/.*",
          entries => {
            val payloads = mutable.ListBuffer[Payload]()
            entries.foreach(entry => { payloads += PayloadParser(entry).parse() })
            request.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), entry.annotations())
          }
        )
      }
    )

    request
  }
}

case class OperationParser(entry: EntryNode) {
  def parse(): Operation = {

    val operation = Operation(entry.ast)
    val entries   = new Entries(entry.ast)

    operation.set(Method, ValueNode(entry.key).string())

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

    operation.set(OperationModel.Request, RequestParser(entries).parse())

    entries.key(
      "responses",
      entry => {
        new Entries(entry.value).regex(
          "\\d{3}",
          entries => {
            val responses = mutable.ListBuffer[Response]()
            entries.foreach(entry => { responses += ResponseParser(entry).parse() })
            operation.set(OperationModel.Responses, AmfArray(responses, Annotations(entry.value)), entry.annotations())
          }
        )
      }
    )

    operation
  }
}

case class ParametersParser(ast: AMFAST) {
  def parse(): Seq[Parameter] = {
    new Entries(ast).entries.values
      .map(entry => ParameterParser(entry).parse())
      .toSeq
  }
}

case class PayloadParser(entry: EntryNode) {
  def parse(): Payload = {
    val payload = Payload(entry.ast)

    payload.set(PayloadModel.MediaType, ValueNode(entry.key).string())

    if (entry.value != null) {
      payload.set(PayloadModel.Schema, ValueNode(entry.value).string())
    }

    payload
  }
}

case class ResponseParser(entry: EntryNode) {
  def parse(): Response = {
    val response = Response(entry.ast)

    val entries = new Entries(entry.value)

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      response.set(ResponseModel.Description, value.string(), entry.annotations())
    })

    entries.key(
      "headers",
      entry => {
        val parameters: Seq[Parameter] = ParametersParser(entry.value).parse()
        response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), entry.annotations())
      }
    )

    entries.key(
      "body",
      entry => {
        new Entries(entry.value).regex(
          ".*/.*",
          entries => {
            val payloads = mutable.ListBuffer[Payload]()
            entries.foreach(entry => { payloads += PayloadParser(entry).parse() })
            response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), entry.annotations())
          }
        )
      }
    )

    val node = ValueNode(entry.key)
    response.set(ResponseModel.Name, node.string())
    response.set(ResponseModel.StatusCode, node.string())
  }
}

case class ParameterParser(entry: EntryNode) {
  def parse(): Parameter = {
    val parameter = Parameter(entry.ast)

    val name = entry.key.content.unquote
    parameter
      .set(ParameterModel.Required, !name.endsWith("?"))
      .set(ParameterModel.Name, ValueNode(entry.key).string())

    val entries = new Entries(entry.value)

    entries.key("description", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Description, value.string(), entry.annotations())
    })

    entries.key("required", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Required, value.boolean(), entry.annotations() + ExplicitField())
    })

    entries.key("type", entry => {
      val value = ValueNode(entry.value)
      parameter.set(ParameterModel.Schema, value.string(), entry.annotations())
    })

    parameter
  }
}

case class LicenseParser(ast: AMFAST) {
  def parse(): License = {
    val license = License(ast)
    val entries = new Entries(ast)

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
    val entries      = new Entries(ast)

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
    val entries      = new Entries(ast)

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

class Entries(ast: AMFAST) {

  def key(keyword: String, fn: (EntryNode => Unit)): Unit = {
    entries.get(keyword) match {
      case Some(entry) => fn(entry)
      case _           =>
    }
  }

  def regex(regex: String, fn: (Seq[EntryNode] => Unit)): Unit = {
    val path: Regex = regex.r
    entries
      .filterKeys({
        case path() => true
        case _      => false
      })
      .values
  }

  var entries: Map[String, EntryNode] = ast.children.map(n => n.head.content.unquote -> EntryNode(n)).toMap

}

case class EntryNode(ast: AMFAST) {

  val key: AMFAST   = ast.head
  val value: AMFAST = if (ast.children.size > 1) ast.last else null

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

  def boolean(): AmfScalar = {
    val content = ast.content.unquote
    AmfScalar(content.toBoolean, annotations())
  }

  private def annotations() = Annotations(ast)
}
