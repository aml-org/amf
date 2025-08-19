package amf.brokergroup.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.brokergroup.internal.plugins.parse.schema.BrokerGroupSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class BrokerGroupBaseUnitClient private[amf] (override protected val configuration: BrokerGroupConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = BrokerGroupSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.BROKER_GROUP
}
