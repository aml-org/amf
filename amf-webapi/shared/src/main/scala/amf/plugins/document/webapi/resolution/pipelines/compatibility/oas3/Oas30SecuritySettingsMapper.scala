package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.ErrorHandler
import amf.plugins.document.webapi.resolution.pipelines.compatibility.common.SecuritySettingsMapper
import amf.plugins.domain.webapi.metamodel.security.OAuth2FlowModel
import amf.plugins.domain.webapi.models.security._

class Oas30SecuritySettingsMapper()(override implicit val errorHandler: ErrorHandler) extends SecuritySettingsMapper {

  def VALID_AUTHORIZATION_GRANTS: List[String] = List("implicit", "clientCredentials", "authorizationCode", "password")

  def createFlowsFromNullFlow(oauth2: OAuth2Settings): Seq[OAuth2Flow] = {
    val grants = oauth2.authorizationGrants
      .map(g => toOasGrant(g.value()))
      .filter(g => VALID_AUTHORIZATION_GRANTS.contains(g))
    val flow = oauth2.flows.head
    grants
      .map(g => (g, flowDeepCopy(flow)))
      .map({
        case (grantName, flow) => flow.withFlow(grantName)
      })
  }

  private def flowDeepCopy(flow: OAuth2Flow): OAuth2Flow = {
    val fields      = flow.fields.copy()
    val annotations = flow.annotations.copy()
    OAuth2Flow(fields, annotations)
  }

  private def toOasGrant(grant: String): String = grant match {
    case "authorization_code" => "authorizationCode"
    case "password"           => "password"
    case "implicit"           => "implicit"
    case "client_credentials" => "clientCredentials"
    case _                    => grant
  }

  // TODO: refactor this. Logic should be simplified
  def fixOauth2(oauth2: OAuth2Settings): Unit = {
    if (oauth2.flows.isEmpty) {
      val flow = oauth2.authorizationGrants.head.option().getOrElse("implicit") match {
        case "authorization_code" => "authorizationCode"
        case "password"           => "password"
        case "implicit"           => "implicit"
        case "client_credentials" => "clientCredentials"
        case _                    => "implicit"
      }
      oauth2.withFlow().withFlow(flow)
    }
    val flow = oauth2.flows.head

    Option(flow.flow.value()) match {
      case Some("implicit") =>
        if (flow.authorizationUri.option().isEmpty) flow.withAuthorizationUri("http://")
        flow.fields.removeField(OAuth2FlowModel.AccessTokenUri)
      case Some("authorizationCode") =>
        if (flow.authorizationUri.option().isEmpty) flow.withAuthorizationUri("http://")
        if (flow.accessTokenUri.option().isEmpty) flow.withAccessTokenUri("http://")
      case Some("password") =>
        if (flow.accessTokenUri.option().isEmpty) flow.withAccessTokenUri("http://")
        flow.fields.removeField(OAuth2FlowModel.AuthorizationUri)
      case Some("clientCredentials") =>
        if (flow.accessTokenUri.option().isEmpty) flow.withAccessTokenUri("http://")
        flow.fields.removeField(OAuth2FlowModel.AuthorizationUri)
      case None => oauth2.withFlows(createFlowsFromNullFlow(oauth2))
      case _    => // ignore
    }
    oauth2.flows.foreach(addPermissiveFlows)
  }

  private def addPermissiveFlows(flow: OAuth2Flow): Unit =
    if (flow.scopes.isEmpty) flow.withScopes(Seq(Scope().withName("*").withDescription("")))
}
