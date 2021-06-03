package amf.plugins.document.webapi

import amf.client.remod.amfcore.plugins.render.AMFRenderPluginAdapter

object Async20RenderPlugin    extends AMFRenderPluginAdapter(Async20Plugin, "application/yaml")
object Oas20RenderPlugin      extends AMFRenderPluginAdapter(Oas20Plugin, "application/json")
object Oas30RenderPlugin      extends AMFRenderPluginAdapter(Oas30Plugin, "application/json") // TODO ARM Should be application/yaml??
object Raml08RenderPlugin     extends AMFRenderPluginAdapter(Raml08Plugin, "application/yaml")
object Raml10RenderPlugin     extends AMFRenderPluginAdapter(Raml10Plugin, "application/yaml")
object PayloadRenderPlugin    extends AMFRenderPluginAdapter(PayloadPlugin, "application/json")
object JsonSchemaRenderPlugin extends AMFRenderPluginAdapter(JsonSchemaPlugin, "application/json")
