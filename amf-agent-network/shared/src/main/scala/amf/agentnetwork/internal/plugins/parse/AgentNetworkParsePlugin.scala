package amf.agentnetwork.internal.plugins.parse

import amf.agentnetwork.internal.plugins.parse.entry.AgentNetworkSchemaVersionEntry
import amf.agentnetwork.internal.plugins.parse.schema.AgentNetworkSchemaLoader
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgentNetwork, Spec}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgentNetworkParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgentNetworkSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = AgentNetworkSchemaVersionEntry(document).nonEmpty

  override def spec: Spec = AgentNetwork
}
