package amf.agentmetadata.internal.plugins.parse

import amf.agentmetadata.internal.plugins.parse.entry.AgentMetadataIdEntry
import amf.agentmetadata.internal.plugins.parse.schema.AgentMetadataSchemaLoader
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgentMetadata, Spec}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgentMetadataParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgentMetadataSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = AgentMetadataIdEntry(document).nonEmpty

  override def spec: Spec = AgentMetadata
}
