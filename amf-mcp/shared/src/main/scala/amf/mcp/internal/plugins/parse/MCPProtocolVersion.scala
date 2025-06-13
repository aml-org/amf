package amf.mcp.internal.plugins.parse

import org.yaml.model.{YMap, YNode, YScalar}

abstract class MCPProtocolVersion(val version: String)

object MCP20241105ProtocolVersion extends MCPProtocolVersion("2024-11-05")
object MCP20250326ProtocolVersion extends MCPProtocolVersion("2025-03-26")

object MCPProtocolVersion {

  def parseProtocolVersion(ast: YNode): Option[MCPProtocolVersion] =
    parseVersionEntry(ast)
      .map(_.value)
      .collect { case scalar: YScalar => scalar.text }
      .flatMap(getProtocolVersionFromString)

  private def getProtocolVersionFromString(text: String): Option[MCPProtocolVersion] = {
    text match {
      case MCP20241105ProtocolVersion.version => Some(MCP20241105ProtocolVersion)
      case MCP20250326ProtocolVersion.version => Some(MCP20250326ProtocolVersion)
      case _                                  => None
    }
  }

  private def parseVersionEntry(ast: YNode): Option[YNode] = {
    ast.value match {
      case map: YMap => map.map.get("protocolVersion")
      case _         => None
    }
  }
}
