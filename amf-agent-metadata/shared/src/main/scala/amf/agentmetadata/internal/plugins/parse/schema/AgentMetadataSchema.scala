package amf.agentmetadata.internal.plugins.parse.schema

import amf.agentmetadata.internal.spec.AgentMetadataSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentMetadataSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = AgentMetadataSchemaContent.content

}
