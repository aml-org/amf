package amf.mcp.internal.plugins.parse

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import org.yaml.model.YMap

object MCPProtocolEntry {

  def apply(root: Root): Option[MCPProtocolVersion] =
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        parsed.document.to[YMap] match {
          case Right(map) => MCPProtocolVersion.parseProtocolVersion(map)
          case Left(_) => None
        }
      case _ => None
    }
}
