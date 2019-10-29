package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.models.security.{ApiKeySettings, OAuth2Settings, SecurityScheme}

class SecuritySettingsMapper()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  def fixOauth2(oauth2: OAuth2Settings): Unit = {
    if (oauth2.authorizationGrants.isEmpty) oauth2.withAuthorizationGrants(Seq("implicit"))

    val flow = oauth2.flows.headOption.getOrElse(oauth2.withFlow())

    if (flow.accessTokenUri.option().isEmpty) flow.withAccessTokenUri("")
    if (flow.authorizationUri.option().isEmpty) flow.withAuthorizationUri("")
  }

  def fixApiKey(security: SecurityScheme, apiKey: ApiKeySettings): Unit = {
    apiKey.in.value() match {
      case "query" =>
        security.withQueryParameter(apiKey.name.option().getOrElse("default")).withSchema(AnyShape())
      case "header" =>
        security.withHeader(apiKey.name.option().getOrElse("default")).withSchema(AnyShape())
      case _ => // ignore
    }
    security.fields.removeField(SecuritySchemeModel.Settings)
  }

  private def fixSettings(security: SecurityScheme): Unit = security.settings match {
    case oauth2: OAuth2Settings => fixOauth2(oauth2)
    case apiKey: ApiKeySettings => fixApiKey(security, apiKey)
    case _                      => // ignore
  }

  private def fixSettings(d: DeclaresModel): Unit = d.declares.foreach {
    case security: SecurityScheme => fixSettings(security)
    case _                        => // ignore
  }

  override def resolve[T <: BaseUnit](model: T): T = model match {
    case d: DeclaresModel =>
      try {
        fixSettings(d)
      } catch {
        case _: Throwable => // ignore: we don't want this to break anything
      }
      model
    case _ => model
  }
}
