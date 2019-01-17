package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.security.OAuth2SettingsModel
import amf.plugins.domain.webapi.models.security._

class SecuritySettingsMapper()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  def fixOauth2(oauth2: OAuth2Settings): Unit = {
    if (oauth2.flow.option().isEmpty) {
      val flow = oauth2.authorizationGrants.head.option().getOrElse("implicit") match {
        case "authorization_code" => "accessCode"
        case "password"           => "password"
        case "implicit"           => "implicit"
        case "client_credentials" => "application"
        case _                    => "implicit"
      }
      oauth2.withFlow(flow)
    }
    oauth2.flow.value() match {
      case "implicit"   =>
        if (oauth2.authorizationUri.option().isEmpty) oauth2.withAuthorizationUri("http://")
        oauth2.fields.removeField(OAuth2SettingsModel.AccessTokenUri)
      case "accessCode" =>
        if (oauth2.authorizationUri.option().isEmpty) oauth2.withAuthorizationUri("http://")
        if (oauth2.accessTokenUri.option().isEmpty) oauth2.withAccessTokenUri("http://")
      case "password"   =>
        if (oauth2.accessTokenUri.option().isEmpty) oauth2.withAccessTokenUri("http://")
        oauth2.fields.removeField(OAuth2SettingsModel.AuthorizationUri)
      case "application"   =>
        if (oauth2.accessTokenUri.option().isEmpty) oauth2.withAccessTokenUri("http://")
        oauth2.fields.removeField(OAuth2SettingsModel.AuthorizationUri)
      case _            => // ignore
    }
    if (oauth2.scopes.isEmpty) oauth2.withScopes(Seq(Scope().withName("*").withDescription("")))
  }

  def fixApiKey(security: SecurityScheme, apiKey: ApiKeySettings): Unit = {
    if (security.queryParameters.nonEmpty) {
      apiKey.withIn("query")
      apiKey.withName(security.queryParameters.head.name.value())
    } else if (security.headers.nonEmpty) {
      apiKey.withIn("header")
      apiKey.withName(security.headers.head.name.value())
    } else {
      apiKey.withIn("query")
      apiKey.withName("")
    }
  }

  private def fixSettings(security: SecurityScheme): Unit = {
    println(security.name)
    println(security.settings)

    security.settings match {
      case oauth2: OAuth2Settings => fixOauth2(oauth2)
      case apiKey: ApiKeySettings => fixApiKey(security, apiKey)
      case null if security.`type`.option().getOrElse("") == "x-amf-apiKey" =>
        fixApiKey(security, security.withApiKeySettings())
      case _                      => // ignore
    }
  }

  private def fixSettings(d: DeclaresModel): Unit = d.declares.foreach {
    case security: SecurityScheme => fixSettings(security)
    case _                        => // ignore
  }

  def removeUnsupportedSchemes(d: DeclaresModel): DeclaresModel = {
    val filteredDeclarations = d.declares.filter {
      case sec: SecurityScheme =>
        sec.settings match {
          case _: OAuth1Settings        => false
          case _: OpenIdConnectSettings => false
          case _                        => true

        }
      case other             => true
    }
    d.withDeclares(filteredDeclarations)
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
