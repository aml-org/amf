package amf.mcp.internal.plugins.render

import amf.core.internal.remote.{Mcp, Spec}
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

object MCPRenderPlugin extends JsonSchemaBasedSpecRenderPlugin {

  override protected def spec: Spec = Mcp

}
