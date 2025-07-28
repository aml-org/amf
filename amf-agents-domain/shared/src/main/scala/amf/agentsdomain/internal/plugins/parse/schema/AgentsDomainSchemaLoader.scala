package amf.agentsdomain.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.{JsonSchemaBasedSpecSchema, JsonSchemaBasedSpecSchemaLoader}

object AgentsDomainSchemaLoader extends JsonSchemaBasedSpecSchemaLoader {

  override protected def schemaProvider: JsonSchemaBasedSpecSchema = AgentsDomainSchema

}
