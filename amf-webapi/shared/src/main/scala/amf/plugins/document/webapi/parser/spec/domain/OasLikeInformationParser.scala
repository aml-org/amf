package amf.plugins.document.webapi.parser.spec.domain
import amf.core.parser.YMapOps
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.common.WebApiBaseSpecParser
import amf.plugins.domain.webapi.metamodel.api.WebApiModel
import amf.plugins.domain.webapi.models.api.Api
import org.yaml.model.{YMap, YMapEntry}

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
