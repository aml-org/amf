package amf.agentdomain.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.agentdomain.internal.plugins.parse.schema.AgentDomainSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AgentDomainBaseUnitClient private[amf] (override protected val configuration: AgentDomainConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = AgentDomainSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENT_DOMAIN
}
