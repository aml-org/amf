package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{Parameter, Payload, Request}
import amf.apicontract.internal.metamodel.domain.RequestModel
import amf.apicontract.internal.spec.common.Parameters
import amf.apicontract.internal.spec.common.parser.{
  OasParametersParser,
  Raml08ParameterParser,
  RamlParametersParser,
  WebApiShapeParserContextAdapter
}
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.spec
import amf.apicontract.internal.spec.spec.toRaml
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.Lazy
import amf.shapes.internal.domain.resolution.ExampleTracking.tracking
import amf.shapes.internal.spec.raml.parser.Raml10TypeParser
import org.yaml.model.{YMap, YMapEntry, YNode}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import amf.core.internal.utils._

case class Oas20RequestParser(map: YMap, adopt: Request => Unit)(implicit ctx: OasWebApiContext) {
  def parse(): Option[Request] = {
    val request = new Lazy[Request](() => {
      val req = Request(map).add(Annotations.virtual())
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
            RamlParametersParser(entry.value.as[YMap],
                                 (p: Parameter) => Unit,
                                 binding = "query")(spec.toRaml(ctx))
              .parse()
          parameters = parameters.add(Parameters(query = queryParameters))
        }
      )

    map
      .key("headers".asOasExtension)
      .foreach(
        entry => {
          entries += entry
          val headers =
            RamlParametersParser(entry.value.as[YMap],
                                 (p: Parameter) => Unit,
                                 binding = "header")(spec.toRaml(ctx))
              .parse()
          parameters = parameters.add(Parameters(header = headers))
        }
      )

    // baseUriParameters from raml08. Only complex parameters will be written here, simple ones will be in the parameters with binding path.
    map.key(
      "baseUriParameters".asOasExtension,
      entry => {
        entry.value.as[YMap].entries.headOption.foreach { paramEntry =>
          entries += paramEntry
          val parameter =
            Raml08ParameterParser(paramEntry, (p: Parameter) => Unit, binding = "path")(
              spec.toRaml(ctx))
              .parse()
          parameters = parameters.add(Parameters(baseUri08 = Seq(parameter)))
        }
      }
    )

    parameters match {
      case Parameters(query, path, header, _, baseUri08, _) =>
        if (query.nonEmpty)
          request.getOrCreate.setWithoutId(RequestModel.QueryParameters,
                                  AmfArray(query, Annotations(entries.head)),
                                  Annotations(entries.head))
        if (header.nonEmpty)
          request.getOrCreate.setWithoutId(RequestModel.Headers,
                                  AmfArray(header, Annotations(entries.head)),
                                  Annotations(entries.head))

        if (path.nonEmpty || baseUri08.nonEmpty)
          request.getOrCreate.setWithoutId(RequestModel.UriParameters,
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
      request.getOrCreate.setWithoutId(RequestModel.Payloads,
                              AmfArray(payloads, Annotations(entries.head.value)),
                              Annotations(entries.head))

    map.key(
      "queryString".asOasExtension,
      queryEntry => {
        Raml10TypeParser(queryEntry, shape => Unit)(
          WebApiShapeParserContextAdapter(toRaml(ctx)))
          .parse()
          .map(s => request.getOrCreate.withQueryString(tracking(s, request.getOrCreate)))
      }
    )

    request.option
  }
}
