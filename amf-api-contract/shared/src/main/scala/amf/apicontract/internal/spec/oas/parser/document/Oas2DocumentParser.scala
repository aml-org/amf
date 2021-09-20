package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import org.yaml.model.YMap

case class Oas2DocumentParser(root: Root)(implicit override val ctx: OasWebApiContext)
    extends OasDocumentParser(root, Spec.OAS20) {

  override def parseWebApi(map: YMap): WebApi = {
    val api = super.parseWebApi(map)

    map.key("consumes", WebApiModel.Accepts in api)
    map.key("produces", WebApiModel.ContentType in api)
    map.key("schemes", WebApiModel.Schemes in api)

    api
  }

  override protected val definitionsKey: String = "definitions"
  override protected val securityKey: String    = "securityDefinitions"
}
