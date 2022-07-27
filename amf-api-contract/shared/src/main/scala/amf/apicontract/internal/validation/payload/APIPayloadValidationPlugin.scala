package amf.apicontract.internal.validation.payload

import amf.apicontract.internal.validation.payload.collector.ShapeFacetsCollector
import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin
import amf.core.client.common.validation.ProfileName
import amf.core.internal.plugins.validation.ValidationInfo
import amf.shapes.internal.validation.payload.BasePayloadValidationPlugin
import amf.shapes.internal.validation.payload.collector.{
  EnumInShapesCollector,
  ExtensionsCollector,
  PayloadsCollector,
  ValidationCandidateCollector
}

object APIPayloadValidationPlugin {

  protected val id: String = this.getClass.getSimpleName

  def apply(profile: ProfileName) = new APIPayloadValidationPlugin(profile)

}

class APIPayloadValidationPlugin(override val profile: ProfileName)
    extends BasePayloadValidationPlugin
    with BaseApiValidationPlugin {

  override val collectors: Seq[ValidationCandidateCollector] =
    Seq(PayloadsCollector, EnumInShapesCollector, ShapeFacetsCollector, ExtensionsCollector)

  override val id: String = APIPayloadValidationPlugin.id

  override def applies(element: ValidationInfo): Boolean = super.applies(element)

}
