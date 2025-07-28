package amf.agentsdomain.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.agentsdomain.internal.plugins.parse.schema.AgentsDomainSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AgentsDomainBaseUnitClient private[amf] (override protected val configuration: AgentsDomainConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = AgentsDomainSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.AGENTS_DOMAIN
}
