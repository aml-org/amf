package amf.agentnetworkmetadata.client.platform

import amf.agentnetworkmetadata.client.scala.{AgentNetworkMetadataBaseUnitClient => InternalAgentNetworkMetadataBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgentNetworkMetadataBaseUnitClient private[amf] (private val _internal: InternalAgentNetworkMetadataBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
