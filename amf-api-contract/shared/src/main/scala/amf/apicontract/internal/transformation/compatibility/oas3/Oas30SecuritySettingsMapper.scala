package amf.apicontract.internal.transformation.compatibility.oas3

import amf.apicontract.internal.transformation.compatibility.common.SecuritySettingsMapper
import amf.apicontract.internal.metamodel.domain.security.OAuth2FlowModel
import amf.apicontract.client.scala.model.domain.security._

class Oas30SecuritySettingsMapper() extends SecuritySettingsMapper {

  def VALID_AUTHORIZATION_GRANTS: List[String] = List("implicit", "clientCredentials", "authorizationCode", "password")

  def createFlowsFromRamlNullFlow(oauth2: OAuth2Settings): Seq[OAuth2Flow] = {
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

  def fixOauth2(oauth2: OAuth2Settings): Unit = {
    if (oauth2.flows.isEmpty) {
      val flows = flowNamesFromGrants(oauth2)
      flows.foreach(oauth2.withFlow().withFlow)
    } else if (hasRamlNullFlow(oauth2)) oauth2.withFlows(createFlowsFromRamlNullFlow(oauth2))

    oauth2.flows.foreach { flow =>
      correctFlow(flow)
      addPermissiveScope(flow)
    }
  }

  private def hasRamlNullFlow(oauth2: OAuth2Settings) = Option(oauth2.flows.head.flow.value()).isEmpty

  private def correctFlow(flow: OAuth2Flow): Unit = {
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
      case _ => // ignore
    }
  }

  private def addPermissiveScope(flow: OAuth2Flow): Unit =
    if (flow.scopes.isEmpty) flow.withScopes(Seq(Scope().withName("*").withDescription("")))

  private def flowNamesFromGrants(oauth2: OAuth2Settings): List[String] = {
    val grants = oauth2.authorizationGrants.toList
    if (grants.isEmpty) List("implicit")
    else grants.map(g => g.value()).map(flowNameMapping)
  }

  private def flowNameMapping(flowName: String): String = flowName match {
    case "authorization_code" => "authorizationCode"
    case "password"           => "password"
    case "implicit"           => "implicit"
    case "client_credentials" => "clientCredentials"
    case _                    => "implicit"
  }
}
