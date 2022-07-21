package amf.shapes.internal.validation.payload

import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.core.internal.plugins.validation.ValidationInfo
import amf.shapes.internal.validation.payload.collector.{
  EnumInShapesCollector,
  ExtensionsCollector,
  PayloadsCollector,
  ValidationCandidateCollector
}
import amf.shapes.internal.validation.plugin.JsonSchemaModelValidationPlugin

object JsonSchemaPayloadValidationPlugin {

  protected val id: String = this.getClass.getSimpleName

  def apply() = new JsonSchemaPayloadValidationPlugin()

}

class JsonSchemaPayloadValidationPlugin() extends BasePayloadValidationPlugin with JsonSchemaModelValidationPlugin {

  override val profile: ProfileName = ProfileNames.JSONSCHEMA

  override val collectors: Seq[ValidationCandidateCollector] =
    Seq(PayloadsCollector, EnumInShapesCollector, ExtensionsCollector)

  override val id: String = JsonSchemaPayloadValidationPlugin.id

  override def applies(element: ValidationInfo): Boolean = super.applies(element)

}
