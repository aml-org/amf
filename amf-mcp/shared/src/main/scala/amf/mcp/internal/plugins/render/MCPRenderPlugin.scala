package amf.mcp.internal.plugins.render

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderConfiguration, RenderInfo}
import amf.core.internal.plugins.syntax.{ASTBuilder, StringDocBuilder}
import amf.core.internal.remote.{Mcp, Syntax}

// Currently not being used. I leave it here to implement the JSON rendering of the JsonLDInstance in a future...
object MCPRenderPlugin extends AMFRenderPlugin {

  override def defaultSyntax(): String = Mcp.mediaType

  override def mediaTypes: Seq[String] = Seq(Syntax.Json.mediaType)

  override def applies(element: RenderInfo): Boolean = true

  override def priority: PluginPriority = NormalPriority

  override val id: String = "mcp-render-plugin"

  override def emit[T](
      unit: BaseUnit,
      builder: ASTBuilder[T],
      renderConfiguration: RenderConfiguration,
      mediaType: String
  ): Boolean = { true } // Not implemented

  override def getDefaultBuilder: ASTBuilder[_] = new StringDocBuilder()

}
