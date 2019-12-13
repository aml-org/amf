package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.parser._
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.domain.webapi.models.WebApi
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
