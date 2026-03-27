package amf.mcp.internal.plugins.parse.schema

import amf.mcp.internal.spec.MCPSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object MCPSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = MCPSchemaContent.content

}
