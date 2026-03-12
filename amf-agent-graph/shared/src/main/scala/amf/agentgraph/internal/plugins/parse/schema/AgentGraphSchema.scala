package amf.agentgraph.internal.plugins.parse.schema

import amf.agentgraph.internal.spec.AgentGraphSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentGraphSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = AgentGraphSchemaContent.content

}
