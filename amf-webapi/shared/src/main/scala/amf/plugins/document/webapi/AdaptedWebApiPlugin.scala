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

object Async20RenderPlugin extends AMFRenderPluginAdapter(Async20Plugin)
object Oas20RenderPlugin   extends AMFRenderPluginAdapter(Oas20Plugin)
object Oas30RenderPlugin   extends AMFRenderPluginAdapter(Oas30Plugin)
object Raml08RenderPlugin  extends AMFRenderPluginAdapter(Raml08Plugin)
object Raml10RenderPlugin  extends AMFRenderPluginAdapter(Raml10Plugin)
object PayloadRenderPlugin extends AMFRenderPluginAdapter(PayloadPlugin)
