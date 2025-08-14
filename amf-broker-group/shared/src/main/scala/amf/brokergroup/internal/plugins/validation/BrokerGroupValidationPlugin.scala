package amf.brokergroup.internal.plugins.validation

import amf.core.client.scala.model.domain.Shape
import amf.brokergroup.internal.plugins.parse.schema.BrokerGroupSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

class BrokerGroupValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {

  override protected val schemaShape: Shape = BrokerGroupSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.BROKER_GROUP
}

object BrokerGroupValidationPlugin {
  def apply(): BrokerGroupValidationPlugin = new BrokerGroupValidationPlugin()
}
