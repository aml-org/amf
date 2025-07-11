package amf.agentfabric.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.agentfabric.internal.plugins.parse.schema.AgentFabricSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AgentFabricBaseUnitClient private[amf] (override protected val configuration: AgentFabricConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = AgentFabricSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_FABRIC
}
