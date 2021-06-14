package amf.plugins.document.apicontract.parser.spec.domain
import amf.core.internal.parser.YMapOps
import amf.plugins.document.apicontract.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.apicontract.parser.spec.common.WebApiBaseSpecParser
import amf.plugins.domain.apicontract.metamodel.api.WebApiModel
import amf.plugins.domain.apicontract.models.api.Api
import org.yaml.model.{YMap, YMapEntry}

import scala.Console.in

case class OasLikeInformationParser(entry: YMapEntry, api: Api, override implicit val ctx: OasLikeWebApiContext)
    extends WebApiBaseSpecParser {

  def parse(): Unit = {
    val info = entry.value.as[YMap]

    ctx.closedShape(api.id, info, "info")

    info.key("title", WebApiModel.Name in api)
    info.key("description", WebApiModel.Description in api)
    info.key("termsOfService", WebApiModel.TermsOfService in api)
    info.key("version", WebApiModel.Version in api)
    info.key("contact", WebApiModel.Provider in api using OrganizationParser.parse)
    info.key("license", WebApiModel.License in api using LicenseParser.parse)
  }

}
