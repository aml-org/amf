package amf.plugins.document.webapi.parser.spec.oas

import amf.core.annotations.VirtualObject
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, _}
import amf.core.utils._
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec
import amf.plugins.document.webapi.parser.spec.declaration.Raml10TypeParser
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.toRaml
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.RequestModel
import amf.plugins.domain.webapi.models.{Parameter, Payload, Request}
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class Oas20RequestParser(map: YMap, adopt: Request => Unit)(implicit ctx: OasWebApiContext) {
  def parse(): Option[Request] = {
    val request = new Lazy[Request](() => {
      val req = Request(map).add(VirtualObject())
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
          OasParametersParser(entry.value.as[Seq[YNode]], request.getOrCreate.id).parse(inRequestOrEndpoint = true))
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
              .map(_.synthesizedBinding("query"))
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
              .map(_.synthesizedBinding("header"))
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
              .synthesizedBinding("path")
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
      entry => {
        entries += entry
        entry.value
          .as[Seq[YNode]]
          .map(value => payloads += OasPayloadParser(value, request.getOrCreate.withPayload).parse())
      }
    )

    if (payloads.nonEmpty)
      request.getOrCreate.set(RequestModel.Payloads,
                              AmfArray(payloads, Annotations(entries.head.value)),
                              Annotations(entries.head))

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
