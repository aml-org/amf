package amf.othercard.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.othercard.internal.plugins.parse.schema.OtherCardSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class OtherCardBaseUnitClient private[amf] (override protected val configuration: OtherCardConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = OtherCardSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.OTHER_CARD
}
