package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.parser.{CustomSyntax, SpecField, SpecNode}
import amf.core.client.common.validation.SeverityLevels

object OasCustomSyntax extends CustomSyntax {
  val flowPossibleFields = Set(
    "type",
    "description",
    "flow",
    "scopes"
  )

  override val nodes: Map[String, SpecNode] = Map(
    "basic" -> SpecNode(
      possibleFields = Set("type", "description")
    ),
    "apiKey" -> SpecNode(
      requiredFields = Set(
        SpecField("in", SeverityLevels.WARNING),
        SpecField("name", SeverityLevels.WARNING)
      ),
      possibleFields = Set("type", "description")
    ),
    "oauth2" -> SpecNode(
      requiredFields = Set(
        SpecField("flow", SeverityLevels.WARNING),
        SpecField("scopes", SeverityLevels.WARNING)
      ),
      possibleFields = Set(
        "type",
        "description",
        "authorizationUrl",
        "tokenUrl",
        "flow",
        "scopes"
      )
    ),
    "implicit" -> SpecNode(
      requiredFields = Set(SpecField("authorizationUrl", SeverityLevels.WARNING)),
      possibleFields = flowPossibleFields
    ),
    "accessCode" -> SpecNode(
      requiredFields = Set(
        SpecField("authorizationUrl", SeverityLevels.WARNING),
        SpecField("tokenUrl", SeverityLevels.WARNING)
      ),
      possibleFields = flowPossibleFields
    ),
    "application" -> SpecNode(
      requiredFields = Set(SpecField("tokenUrl", SeverityLevels.WARNING)),
      possibleFields = flowPossibleFields
    ),
    "password" -> SpecNode(
      requiredFields = Set(SpecField("tokenUrl", SeverityLevels.WARNING)),
      possibleFields = flowPossibleFields
    )
  )
}
