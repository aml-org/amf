package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.Lazy
import amf.shapes.internal.spec.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.apicontract.parser.spec.domain.{OasParametersParser, Parameters}
import amf.plugins.domain.apicontract.metamodel.RequestModel
import amf.plugins.domain.apicontract.models.Request
import org.yaml.model.{YMap, YNode}

case class Oas30ParametersParser(map: YMap, producer: () => Request)(implicit ctx: OasWebApiContext) {

  def parseParameters(): Unit = {
    val request = new Lazy[Request](producer)
    map
      .key("parameters")
      .foreach { entry =>
        val parameters =
          OasParametersParser(entry.value.as[Seq[YNode]], request.getOrCreate.id).parse(inRequestOrEndpoint = true)
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
