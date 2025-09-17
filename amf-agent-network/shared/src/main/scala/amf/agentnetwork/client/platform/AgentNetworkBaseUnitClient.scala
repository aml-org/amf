package amf.agentnetwork.client.platform

import amf.agentnetwork.client.scala.{AgentNetworkBaseUnitClient => InternalAgentNetworkBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgentNetworkBaseUnitClient private[amf] (private val _internal: InternalAgentNetworkBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
