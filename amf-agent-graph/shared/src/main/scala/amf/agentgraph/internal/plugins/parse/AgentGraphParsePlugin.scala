package amf.agentgraph.internal.plugins.parse

import amf.agentgraph.internal.plugins.parse.entry.AgentGraphIdEntry
import amf.agentgraph.internal.plugins.parse.schema.AgentGraphSchemaLoader
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AgentGraph, Spec}
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object AgentGraphParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = AgentGraphSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = AgentGraphIdEntry(document).nonEmpty

  override def spec: Spec = AgentGraph
}
