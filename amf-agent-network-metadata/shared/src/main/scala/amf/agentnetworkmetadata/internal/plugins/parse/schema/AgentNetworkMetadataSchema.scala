package amf.agentnetworkmetadata.internal.plugins.parse.schema

import amf.agentnetworkmetadata.internal.spec.AgentNetworkMetadataSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgentNetworkMetadataSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = AgentNetworkMetadataSchemaContent.content

}
