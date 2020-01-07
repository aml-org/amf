package amf.plugins.document.webapi.parser.spec.domain
import amf.plugins.document.webapi.parser.spec.common.WebApiBaseSpecParser
import amf.plugins.domain.webapi.models.WebApi
import amf.plugins.domain.webapi.metamodel.WebApiModel
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import org.yaml.model.{YMap, YMapEntry}
import amf.core.parser.YMapOps

case class OasLikeInformationParser(entry: YMapEntry, api: WebApi, override implicit val ctx: OasLikeWebApiContext)
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
