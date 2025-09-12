package amf.llmmetadata.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.llmmetadata.internal.plugins.parse.schema.LLMMetadataSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class LLMMetadataValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = LLMMetadataSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.LLM_METADATA
}

object LLMMetadataValidationPlugin {
  def apply(): LLMMetadataValidationPlugin = new LLMMetadataValidationPlugin()
}
