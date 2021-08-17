package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.common.parser.WebApiBaseSpecParser
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.core.internal.parser.YMapOps
import org.yaml.model.{YMap, YMapEntry}

case class OasLikeInformationParser(entry: YMapEntry, api: Api, override implicit val ctx: OasLikeWebApiContext)
    extends WebApiBaseSpecParser {

  def parse(): Unit = {
    val info = entry.value.as[YMap]

    ctx.closedShape(api, info, "info")

    info.key("title", WebApiModel.Name in api)
    info.key("description", WebApiModel.Description in api)
    info.key("termsOfService", WebApiModel.TermsOfService in api)
    info.key("version", WebApiModel.Version in api)
    info.key("contact", WebApiModel.Provider in api using OrganizationParser.parse)
    info.key("license", WebApiModel.License in api using LicenseParser.parse)
  }

}
