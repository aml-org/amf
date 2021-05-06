package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.errorhandling.ErrorHandler
import amf.plugins.document.webapi.resolution.pipelines.compatibility.common.SecuritySettingsMapper
import amf.plugins.domain.webapi.metamodel.security.OAuth2FlowModel
import amf.plugins.domain.webapi.models.security._

class Oas20SecuritySettingsMapper() extends SecuritySettingsMapper {

  def fixOauth2(oauth2: OAuth2Settings): Unit = {
    if (oauth2.flows.isEmpty) {
      val flow = oauth2.authorizationGrants.head.option().getOrElse("implicit") match {
        case "authorization_code" => "accessCode"
        case "password"           => "password"
        case "implicit"           => "implicit"
        case "client_credentials" => "application"
        case _                    => "implicit"
      }
      oauth2.withFlow().withFlow(flow)
    }
    val flow = oauth2.flows.head

    flow.flow.value() match {
      case "implicit" =>
        if (flow.authorizationUri.option().isEmpty) flow.withAuthorizationUri("http://")
        flow.fields.removeField(OAuth2FlowModel.AccessTokenUri)
      case "accessCode" =>
        if (flow.authorizationUri.option().isEmpty) flow.withAuthorizationUri("http://")
        if (flow.accessTokenUri.option().isEmpty) flow.withAccessTokenUri("http://")
      case "password" =>
        if (flow.accessTokenUri.option().isEmpty) flow.withAccessTokenUri("http://")
        flow.fields.removeField(OAuth2FlowModel.AuthorizationUri)
      case "application" =>
        if (flow.accessTokenUri.option().isEmpty) flow.withAccessTokenUri("http://")
        flow.fields.removeField(OAuth2FlowModel.AuthorizationUri)
      case _ => // ignore
    }
    if (flow.scopes.isEmpty) flow.withScopes(Seq(Scope().withName("*").withDescription("")))
  }
}
