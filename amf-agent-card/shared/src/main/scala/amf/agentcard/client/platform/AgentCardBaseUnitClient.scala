package amf.agentcard.client.platform

import amf.agentcard.client.scala.{AgentCardBaseUnitClient => InternalAgentCardBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgentCardBaseUnitClient private[amf] (private val _internal: InternalAgentCardBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
