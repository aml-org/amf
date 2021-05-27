package amf.plugins.document.webapi

import amf.client.remod.amfcore.plugins.parse.AMFParsePluginAdapter
import amf.client.remod.amfcore.plugins.render.AMFRenderPluginAdapter

object Async20ParsePlugin              extends AMFParsePluginAdapter(Async20Plugin)
object Oas20ParsePlugin                extends AMFParsePluginAdapter(Oas20Plugin)
object Oas30ParsePlugin                extends AMFParsePluginAdapter(Oas30Plugin)
object Raml08ParsePlugin               extends AMFParsePluginAdapter(Raml08Plugin)
object Raml10ParsePlugin               extends AMFParsePluginAdapter(Raml10Plugin)
object PayloadParsePlugin              extends AMFParsePluginAdapter(PayloadPlugin)
object JsonSchemaParsePlugin           extends AMFParsePluginAdapter(JsonSchemaPlugin)
object ExternalJsonYamlRefsParsePlugin extends AMFParsePluginAdapter(ExternalJsonYamlRefsPlugin)

object Async20RenderPlugin    extends AMFRenderPluginAdapter(Async20Plugin, "application/yaml")
object Oas20RenderPlugin      extends AMFRenderPluginAdapter(Oas20Plugin, "application/json")
object Oas30RenderPlugin      extends AMFRenderPluginAdapter(Oas30Plugin, "application/yaml")
object Raml08RenderPlugin     extends AMFRenderPluginAdapter(Raml08Plugin, "application/yaml")
object Raml10RenderPlugin     extends AMFRenderPluginAdapter(Raml10Plugin, "application/yaml")
object PayloadRenderPlugin    extends AMFRenderPluginAdapter(PayloadPlugin, "application/json")
object JsonSchemaRenderPlugin extends AMFRenderPluginAdapter(JsonSchemaPlugin, "application/json")
