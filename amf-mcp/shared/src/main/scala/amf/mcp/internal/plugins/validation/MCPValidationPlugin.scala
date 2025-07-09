package amf.mcp.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.mcp.internal.plugins.parse.schema.MCPSchemaLoader
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class MCPValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = MCPSchemaLoader.schema

}

object MCPValidationPlugin {
  def apply(): MCPValidationPlugin = new MCPValidationPlugin()
}
