package amf.agentfabric.client.platform

import amf.agentfabric.client.scala.{AgentFabricBaseUnitClient => InternalAgentFabricBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgentFabricBaseUnitClient private[amf] (private val _internal: InternalAgentFabricBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
