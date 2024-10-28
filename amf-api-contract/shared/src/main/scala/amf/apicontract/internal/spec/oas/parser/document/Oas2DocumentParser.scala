package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import org.yaml.model.YMap

class Oas2DocumentParser(root: Root, spec: Spec = Spec.OAS20)(implicit override val ctx: OasWebApiContext)
    extends OasDocumentParser(root, spec) {

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

object Oas2DocumentParser {
  def apply(root: Root, spec: Spec = Spec.OAS20)(implicit ctx: OasWebApiContext): Oas2DocumentParser =
    new Oas2DocumentParser(root, spec)
}
