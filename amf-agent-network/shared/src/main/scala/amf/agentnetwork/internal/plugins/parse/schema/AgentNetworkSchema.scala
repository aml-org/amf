package amf.agentnetwork.internal.plugins.parse.schema

import amf.agentnetwork.internal.spec.AgentNetworkSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentNetworkSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = AgentNetworkSchemaContent.content
}
