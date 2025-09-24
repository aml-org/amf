package amf.agentnetwork.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.agentnetwork.internal.plugins.parse.schema.AgentNetworkSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AgentNetworkBaseUnitClient private[amf] (override protected val configuration: AgentNetworkConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = AgentNetworkSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_NETWORK
}
