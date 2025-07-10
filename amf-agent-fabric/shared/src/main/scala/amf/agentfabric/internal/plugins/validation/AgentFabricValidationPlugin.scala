package amf.agentfabric.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.agentfabric.internal.plugins.parse.schema.AgentFabricSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgentFabricValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgentFabricSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_FABRIC
}

object AgentFabricValidationPlugin {
  def apply(): AgentFabricValidationPlugin = new AgentFabricValidationPlugin()
}
