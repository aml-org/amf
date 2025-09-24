package amf.llmmetadata.internal.plugins.render

import amf.core.internal.remote.{LLMMetadata, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object LLMMetadataRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = LLMMetadata

}
