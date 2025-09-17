package amf.agentnetwork.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.agentnetwork.internal.plugins.parse.schema.AgentNetworkSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgentNetworkValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgentNetworkSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_NETWORK
}

object AgentNetworkValidationPlugin {
  def apply(): AgentNetworkValidationPlugin = new AgentNetworkValidationPlugin()
}
