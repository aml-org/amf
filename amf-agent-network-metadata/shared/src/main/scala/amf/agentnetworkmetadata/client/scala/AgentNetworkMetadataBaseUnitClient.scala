package amf.agentnetworkmetadata.client.scala

import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.agentnetworkmetadata.internal.plugins.parse.schema.AgentNetworkMetadataSchemaLoader
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AgentNetworkMetadataBaseUnitClient private[amf] (override protected val configuration: AgentNetworkMetadataConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = AgentNetworkMetadataSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_NETWORK_METADATA
}
