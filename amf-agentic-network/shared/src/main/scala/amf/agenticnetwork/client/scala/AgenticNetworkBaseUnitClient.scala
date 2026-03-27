package amf.agenticnetwork.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.agenticnetwork.internal.plugins.parse.schema.AgenticNetworkSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AgenticNetworkBaseUnitClient private[amf] (override protected val configuration: AgenticNetworkConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = AgenticNetworkSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENTIC_NETWORK
}
