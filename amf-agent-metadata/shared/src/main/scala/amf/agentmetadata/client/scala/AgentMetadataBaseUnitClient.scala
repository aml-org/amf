package amf.agentmetadata.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.agentmetadata.internal.plugins.parse.schema.AgentMetadataSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AgentMetadataBaseUnitClient private[amf] (override protected val configuration: AgentMetadataConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = AgentMetadataSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_METADATA
}
