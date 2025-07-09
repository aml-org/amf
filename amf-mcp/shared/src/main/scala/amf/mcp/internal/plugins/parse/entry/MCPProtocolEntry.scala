package amf.mcp.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntry, IdEntryVersion}

object MCPProtocolEntry extends IdEntry {
  override protected def versionHandler: IdEntryVersion = MCPProtocolVersion
}
