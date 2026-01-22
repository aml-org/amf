package amf.agentgraph.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.{JsonSchemaBasedSpecSchema, JsonSchemaBasedSpecSchemaLoader}

object AgentGraphSchemaLoader extends JsonSchemaBasedSpecSchemaLoader {

  override protected def schemaProvider: JsonSchemaBasedSpecSchema = AgentGraphSchema

}
