package amf.plugins.document.webapi.parser.spec.common

import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Field
import amf.plugins.domain.webapi.metamodel.security.OAuth2FlowModel
import amf.plugins.domain.webapi.models.security.OAuth2Flow
import amf.validations.ParserSideValidations.MissingOAuthFlowField

object OAuth2FlowValidations {
  case class ParticularFlow(name: String, requiredFields: List[FlowField])
  case class FlowField(name: String, field: Field)

  val authorizationUrl: FlowField = FlowField("authorizationUrl", OAuth2FlowModel.AuthorizationUri)
  val tokenUrl: FlowField         = FlowField("tokenUrl", OAuth2FlowModel.AccessTokenUri)
  val refreshUrl: FlowField       = FlowField("refreshUrl", OAuth2FlowModel.RefreshUri)
  val scopes: FlowField           = FlowField("scopes", OAuth2FlowModel.Scopes)

  val requiredFieldsPerFlow: Map[String, ParticularFlow] = Seq(
    // oas20 & 0as30
    ParticularFlow("implicit", List(authorizationUrl, scopes)),
    ParticularFlow("password", List(tokenUrl, scopes)),
    // oas30
    ParticularFlow("clientCredentials", List(tokenUrl, scopes)),
    ParticularFlow("authorizationCode", List(authorizationUrl, tokenUrl, scopes)),
    // oas20
    ParticularFlow("application", List(tokenUrl, scopes)),
    ParticularFlow("accessCode", List(authorizationUrl, tokenUrl, scopes))
  ).map(x => (x.name, x)).toMap

  def validateFlowFields(flow: OAuth2Flow, errorHandler: ErrorHandler): Unit = {
    val flowName            = flow.flow.value()
    val requiredFlowsOption = requiredFieldsPerFlow.get(flowName)
    if (requiredFlowsOption.nonEmpty) {
      val missingFields =
        requiredFlowsOption.get.requiredFields.filter(flowField => flow.fields.entry(flowField.field).isEmpty)
      missingFields.foreach { flowField =>
        {
          val message = s"Missing ${flowField.name} for $flowName flow"
          errorHandler.violation(MissingOAuthFlowField, flow.id, message)
        }
      }
    }
  }
}
