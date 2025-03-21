package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{Parameter, Request}
import amf.apicontract.internal.metamodel.domain.RequestModel._
import amf.apicontract.internal.spec.common.Parameters
import amf.apicontract.internal.spec.common.parser.OasParametersParser
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.Lazy
import org.yaml.model.{YMap, YNode}

case class Oas30ParametersParser(map: YMap, producer: () => Request)(implicit ctx: OasWebApiContext) {

  def parseParameters(): Unit = {
    val request = new Lazy[Request](producer)
    map
      .key("parameters")
      .foreach { entry =>
        val req = request.getOrCreate
        val parameters =
          OasParametersParser(entry.value.as[Seq[YNode]], req.id).parse(inRequestOrEndpoint = true)
        if (parameters.nonEmpty) {
          val ann = Annotations(entry)
          if (req.annotations.size == 0) req.annotations.overrideWith(ann)
          parameters match {
            case Parameters(query, path, header, cookie, baseUri08, _) =>
              def setParameters(params: Seq[Parameter], field: Field): Unit =
                req.setWithoutId(field, AmfArray(params, ann), ann)
              if (query.nonEmpty) setParameters(query, QueryParameters)
              if (header.nonEmpty) setParameters(header, Headers)
              if (path.nonEmpty || baseUri08.nonEmpty) setParameters(path ++ baseUri08, UriParameters)
              if (cookie.nonEmpty) setParameters(cookie, CookieParameters)
          }
        }
      }
  }
}
