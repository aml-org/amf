package amf.agentcard.internal.plugins.parse.schema

import amf.agentcard.internal.spec.AgentCardSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentCardSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = AgentCardSchemaContent.content
}
