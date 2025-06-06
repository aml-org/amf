package amf.mcp.client.scala

import amf.mcp.internal.plugins.parse.MCPParsePlugin
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.internal.plugins.parser.AMFJsonLDSchemaGraphParsePlugin
import amf.shapes.internal.plugins.render.AMFJsonLDSchemaGraphRenderPlugin

object MCPConfiguration {
  def MCP(): ShapesConfiguration =
    ShapesConfiguration
      .predefined()
      .withPlugins(List(MCPParsePlugin, AMFJsonLDSchemaGraphRenderPlugin, AMFJsonLDSchemaGraphParsePlugin))
}
