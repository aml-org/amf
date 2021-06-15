package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.internal.parser.{Root, YMapOps}
import amf.shapes.internal.spec.contexts.parser.oas.OasWebApiContext
import amf.plugins.domain.apicontract.metamodel.api.WebApiModel
import amf.plugins.domain.apicontract.models.api.WebApi
import org.yaml.model.YMap

case class Oas2DocumentParser(root: Root)(implicit override val ctx: OasWebApiContext)
    extends OasDocumentParser(root) {

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
