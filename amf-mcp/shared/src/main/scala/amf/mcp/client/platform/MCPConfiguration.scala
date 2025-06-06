package amf.mcp.client.platform

import amf.mcp.client.scala.{MCPConfiguration => InternalMCPConfiguration}
import amf.shapes.client.platform.ShapesConfiguration

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("MCPConfiguration")
object MCPConfiguration {

  def MCP(): ShapesConfiguration = new ShapesConfiguration(InternalMCPConfiguration.MCP())
}
