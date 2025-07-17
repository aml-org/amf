package amf.agentcard.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.agentcard.internal.plugins.parse.schema.AgentCardSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgentCardValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgentCardSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_CARD
}

object AgentCardValidationPlugin {
  def apply(): AgentCardValidationPlugin = new AgentCardValidationPlugin()
}
