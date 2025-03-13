package amf.apicontract.internal.transformation.compatibility.oas

import amf.apicontract.client.scala.model.domain.security._
import amf.apicontract.internal.metamodel.domain.security.OAuth2FlowModel
import amf.apicontract.internal.transformation.compatibility.common.SecuritySettingsMapper
import amf.core.internal.metamodel.Field

class Oas20SecuritySettingsMapper() extends SecuritySettingsMapper {

  /** Fixes OAuth2 settings based on OpenAPI 2.0 conventions. Maps OAuth2 grant types to flow types and ensures required
    * URIs are set.
    *
    * @param oauth2
    *   The OAuth2Settings to fix
    */
  def fixOauth2(oauth2: OAuth2Settings): Unit = {
    // Map from OAuth 2.0 standard grant types to OAS 2.0 flow names
    val grantToFlowMapping = Map(
      "authorization_code" -> "accessCode",
      "password"           -> "password",
      "implicit"           -> "implicit",
      "client_credentials" -> "application"
    )

    // Determine flow type from authorization grants, defaulting to "implicit"
    val flowString = oauth2.authorizationGrants.headOption
      .flatMap(_.option())
      .flatMap(grant => grantToFlowMapping.get(grant))
      .getOrElse("implicit")

    // Update flows based on existence
    oauth2.flows match {
      case flows if flows.isEmpty =>
        oauth2.withFlow().withFlow(flowString)
      case flows =>
        flows.foreach { f =>
          if (Option(f.flow.value()).forall(_.isEmpty)) f.withFlow(flowString)
        }
    }

    // Configure the first flow based on its type
    oauth2.flows.headOption.foreach { flow =>
      configureFlow(flow, flowString)
    }
  }

  /** Configures a flow based on its type, setting required URIs and removing irrelevant fields.
    *
    * @param flow
    *   The flow to configure
    * @param flowType
    *   The type of flow (implicit, accessCode, password, application)
    */
  private def configureFlow(flow: OAuth2Flow, flowType: String): Unit = {
    // Default placeholder URI when required but not provided
    val defaultUri = "http://"

    val flowConfig = FlowConfiguration(flow.flow.value()) match {
      case FlowType.Implicit =>
        FlowConfiguration(
          needsAuthUri = true,
          needsTokenUri = false,
          defaultAuthUri = Some(defaultUri),
          defaultTokenUri = None,
          fieldsToRemove = Seq(OAuth2FlowModel.AccessTokenUri)
        )
      case FlowType.AccessCode =>
        FlowConfiguration(
          needsAuthUri = true,
          needsTokenUri = true,
          defaultAuthUri = Some(defaultUri),
          defaultTokenUri = Some(defaultUri),
          fieldsToRemove = Seq()
        )
      case FlowType.Password | FlowType.Application =>
        FlowConfiguration(
          needsAuthUri = false,
          needsTokenUri = true,
          defaultAuthUri = None,
          defaultTokenUri = Some(defaultUri),
          fieldsToRemove = Seq(OAuth2FlowModel.AuthorizationUri)
        )
      case _ => FlowConfiguration.Default
    }

    // Apply configuration
    if (flowConfig.needsAuthUri && flow.authorizationUri.option().isEmpty) {
      flowConfig.defaultAuthUri.foreach(uri => flow.withAuthorizationUri(uri))
    }

    if (flowConfig.needsTokenUri && flow.accessTokenUri.option().isEmpty) {
      flowConfig.defaultTokenUri.foreach(uri => flow.withAccessTokenUri(uri))
    }

    flowConfig.fieldsToRemove.foreach(field => flow.fields.removeField(field))

    // Ensure scopes exist
    if (flow.scopes.isEmpty) {
      flow.withScopes(Seq(Scope().withName("*").withDescription("")))
    }
  }

  /** Represents the possible flow types in OAuth 2.0 */
  private object FlowType {
    val Implicit    = "implicit"
    val AccessCode  = "accessCode"
    val Password    = "password"
    val Application = "application"
  }

  /** Configuration for a specific flow type */
  private case class FlowConfiguration(
      needsAuthUri: Boolean,
      needsTokenUri: Boolean,
      defaultAuthUri: Option[String],
      defaultTokenUri: Option[String],
      fieldsToRemove: Seq[Field]
  )

  private object FlowConfiguration {
    def apply(flowType: String): String = flowType

    val Default: FlowConfiguration = FlowConfiguration(
      needsAuthUri = false,
      needsTokenUri = false,
      defaultAuthUri = None,
      defaultTokenUri = None,
      fieldsToRemove = Seq()
    )
  }
}
