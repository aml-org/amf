package amf.llmmetadata.internal.plugins.parse.schema

import amf.shapes.internal.plugins.parser.schema.{JsonSchemaBasedSpecSchema, JsonSchemaBasedSpecSchemaLoader}

object LLMMetadataSchemaLoader extends JsonSchemaBasedSpecSchemaLoader {

  override protected def schemaProvider: JsonSchemaBasedSpecSchema = LLMMetadataSchema

}
