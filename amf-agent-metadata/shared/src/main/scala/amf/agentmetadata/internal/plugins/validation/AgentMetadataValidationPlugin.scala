package amf.agentmetadata.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.agentmetadata.internal.plugins.parse.schema.AgentMetadataSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgentMetadataValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgentMetadataSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_METADATA
}

object AgentMetadataValidationPlugin {
  def apply(): AgentMetadataValidationPlugin = new AgentMetadataValidationPlugin()
}
