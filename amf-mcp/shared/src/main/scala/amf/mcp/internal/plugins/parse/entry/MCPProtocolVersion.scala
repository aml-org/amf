package amf.mcp.internal.plugins.parse.entry

import amf.shapes.internal.plugins.parser.entry.{IdEntryVersion, IdVersion}

abstract class MCPProtocolVersion(override val version: String) extends IdVersion(version)

object MCP20241105ProtocolVersion extends MCPProtocolVersion("2024-11-05")
object MCP20250326ProtocolVersion extends MCPProtocolVersion("2025-03-26")

object MCPProtocolVersion extends IdEntryVersion {

  override protected val idKey: String = "protocolVersion"

  override protected def getIdVersionFromString(text: String): Option[IdVersion] = {
    text match {
      case MCP20241105ProtocolVersion.version => Some(MCP20241105ProtocolVersion)
      case MCP20250326ProtocolVersion.version => Some(MCP20250326ProtocolVersion)
      case _                                  => None
    }
  }
}
