package amf.mcp.internal.plugins.parse

import amf.core.internal.parser.Root
import amf.core.internal.remote.{Mcp, Spec}
import amf.mcp.internal.plugins.parse.entry.MCPProtocolEntry
import amf.mcp.internal.plugins.parse.schema.MCPSchemaLoader
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

object MCPParsePlugin extends JsonSchemaBasedSpecParsePlugin {

  override protected val specSchema: JsonSchemaDocument = MCPSchemaLoader.doc

  override protected def existsSpecEntry(document: Root): Boolean = MCPProtocolEntry(document).nonEmpty

  override def spec: Spec = Mcp
}
