package amf.apicontract.internal.spec.oas.parser

import amf.apicontract.client.scala.model.domain.Request
import amf.apicontract.internal.metamodel.domain.RequestModel
import amf.apicontract.internal.spec.common.Parameters
import amf.apicontract.internal.spec.common.parser.OasParametersParser
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.Lazy
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
