package amf.agenticnetwork.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.agenticnetwork.internal.plugins.parse.schema.AgenticNetworkSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class AgenticNetworkValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = AgenticNetworkSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENTIC_NETWORK
}

object AgenticNetworkValidationPlugin {
  def apply(): AgenticNetworkValidationPlugin = new AgenticNetworkValidationPlugin()
}
