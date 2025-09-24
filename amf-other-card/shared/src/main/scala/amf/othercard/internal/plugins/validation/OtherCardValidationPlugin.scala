package amf.othercard.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.othercard.internal.plugins.parse.schema.OtherCardSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class OtherCardValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = OtherCardSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.OTHER_CARD
}

object OtherCardValidationPlugin {
  def apply(): OtherCardValidationPlugin = new OtherCardValidationPlugin()
}
