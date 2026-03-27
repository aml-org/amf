package amf.agenticnetwork.internal.plugins.parse.schema

import amf.agenticnetwork.internal.spec.AgenticNetworkSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object AgenticNetworkSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = AgenticNetworkSchemaContent.content

}
