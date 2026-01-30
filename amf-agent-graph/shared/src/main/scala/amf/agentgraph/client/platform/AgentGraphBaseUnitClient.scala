package amf.agentgraph.client.platform

import amf.agentgraph.client.scala.{AgentGraphBaseUnitClient => InternalAgentGraphBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgentGraphBaseUnitClient private[amf] (private val _internal: InternalAgentGraphBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
