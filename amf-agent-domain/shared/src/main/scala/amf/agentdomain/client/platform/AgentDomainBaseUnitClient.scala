package amf.agentdomain.client.platform

import amf.agentdomain.client.scala.{AgentDomainBaseUnitClient => InternalAgentDomainBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgentDomainBaseUnitClient private[amf] (private val _internal: InternalAgentDomainBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
