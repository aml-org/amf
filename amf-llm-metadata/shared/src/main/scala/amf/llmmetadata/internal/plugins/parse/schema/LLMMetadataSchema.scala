package amf.llmmetadata.internal.plugins.parse.schema

import amf.llmmetadata.internal.spec.LLMMetadataSchemaContent
import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

object LLMMetadataSchema extends JsonSchemaBasedSpecSchema {

  override def schema: String = LLMMetadataSchemaContent.content

}
