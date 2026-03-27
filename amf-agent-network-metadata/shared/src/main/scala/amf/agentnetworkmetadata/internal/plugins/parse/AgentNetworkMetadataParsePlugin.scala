package amf.agentnetworkmetadata.internal.plugins.parse

import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgentNetworkMetadata, Spec}
import amf.agentnetworkmetadata.internal.plugins.parse.entry.AgentNetworkMetadataIdEntry
import amf.agentnetworkmetadata.internal.plugins.parse.schema.AgentNetworkMetadataSchemaLoader
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgentNetworkMetadataParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgentNetworkMetadataSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = AgentNetworkMetadataIdEntry(document).nonEmpty

  override def spec: Spec = AgentNetworkMetadata
}
