package amf.agenticnetwork.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.{JsonSchemaBasedSpecSchema, JsonSchemaBasedSpecSchemaLoader}

object AgenticNetworkSchemaLoader extends JsonSchemaBasedSpecSchemaLoader {

  override protected def schemaProvider: JsonSchemaBasedSpecSchema = AgenticNetworkSchema

}
