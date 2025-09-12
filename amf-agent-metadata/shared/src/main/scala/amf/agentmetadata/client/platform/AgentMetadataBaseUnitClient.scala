package amf.agentmetadata.client.platform

import amf.agentmetadata.client.scala.{AgentMetadataBaseUnitClient => InternalAgentMetadataBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class AgentMetadataBaseUnitClient private[amf] (private val _internal: InternalAgentMetadataBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
