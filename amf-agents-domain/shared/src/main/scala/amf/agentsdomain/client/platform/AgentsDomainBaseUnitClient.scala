package amf.agentsdomain.client.platform

import amf.agentsdomain.client.scala.{AgentsDomainBaseUnitClient => InternalAgentsDomainBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgentsDomainBaseUnitClient private[amf] (private val _internal: InternalAgentsDomainBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
