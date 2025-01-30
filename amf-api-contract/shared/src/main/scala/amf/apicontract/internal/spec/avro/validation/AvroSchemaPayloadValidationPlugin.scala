package amf.apicontract.internal.spec.avro.validation

import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.core.internal.plugins.validation.ValidationInfo
import amf.shapes.internal.validation.payload.BasePayloadValidationPlugin
import amf.shapes.internal.validation.payload.collector._

object AvroSchemaPayloadValidationPlugin {

  protected val id: String = this.getClass.getSimpleName

  def apply() = new AvroSchemaPayloadValidationPlugin()

}

class AvroSchemaPayloadValidationPlugin extends BasePayloadValidationPlugin with AvroSchemaModelValidationPlugin {

  override val profile: ProfileName = ProfileNames.AVROSCHEMA

  override val collectors: Seq[ValidationCandidateCollector] =
    Seq(PayloadsCollector, EnumInShapesCollector, ExtensionsCollector, DiscriminatorValuesCollector)

  override val id: String = AvroSchemaPayloadValidationPlugin.id

  override def applies(element: ValidationInfo): Boolean = super.applies(element)

}
