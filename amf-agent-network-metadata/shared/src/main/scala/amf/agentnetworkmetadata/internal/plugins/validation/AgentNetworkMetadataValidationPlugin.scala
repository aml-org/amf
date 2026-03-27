package amf.agentnetworkmetadata.internal.plugins.validation

import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.core.client.scala.model.domain.Shape
import amf.agentnetworkmetadata.internal.plugins.parse.schema.AgentNetworkMetadataSchemaLoader
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgentNetworkMetadataValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgentNetworkMetadataSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_NETWORK_METADATA

}

object AgentNetworkMetadataValidationPlugin {
  def apply(): AgentNetworkMetadataValidationPlugin = new AgentNetworkMetadataValidationPlugin()
}
