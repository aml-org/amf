package amf.apicontract.internal.spec.oas.parser.document

import amf.aml.internal.parse.dialects.DialectAstOps.DialectYMapOps
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Spec
import org.yaml.model.YMap

class Oas31DocumentParser(root: Root, spec: Spec = Spec.OAS31)(implicit override val ctx: OasWebApiContext)
    extends Oas3DocumentParser(root, spec) {

  override def parseWebApi(map: YMap): WebApi = {
    val api = super.parseWebApi(map)
    map
      .key("webhooks")
      .foreach(webhooksEntry => {
        val webhooksYMap = webhooksEntry.value.as[YMap]
        val endpoints = webhooksYMap.entries.foldLeft(List[EndPoint]())((acc, entry) => {
          acc ++ ctx.factory.endPointParser(entry, api.id, acc).parse()
        })
        api.setWithoutId(
          WebApiModel.Webhooks,
          AmfArray(endpoints, Annotations(webhooksEntry.value)),
          Annotations(webhooksEntry)
        )
      })
    api
  }
}

object Oas31DocumentParser {
  def apply(root: Root, spec: Spec = Spec.OAS31)(implicit ctx: OasWebApiContext): Oas31DocumentParser =
    new Oas31DocumentParser(root, spec)
}
