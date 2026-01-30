package amf.agentgraph.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.agentgraph.internal.plugins.parse.schema.AgentGraphSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgentGraphValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgentGraphSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_GRAPH
}

object AgentGraphValidationPlugin {
  def apply(): AgentGraphValidationPlugin = new AgentGraphValidationPlugin()
}
