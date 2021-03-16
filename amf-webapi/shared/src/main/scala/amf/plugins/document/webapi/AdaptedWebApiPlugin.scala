package amf.plugins.document.webapi

import amf.client.remod.amfcore.plugins.parse.AMFParsePluginAdapter

object Async20ParsePlugin              extends AMFParsePluginAdapter(Async20Plugin)
object Oas20ParsePlugin                extends AMFParsePluginAdapter(Oas20Plugin)
object Oas30ParsePlugin                extends AMFParsePluginAdapter(Oas30Plugin)
object Raml08ParsePlugin               extends AMFParsePluginAdapter(Raml08Plugin)
object Raml10ParsePlugin               extends AMFParsePluginAdapter(Raml10Plugin)
object PayloadParsePlugin              extends AMFParsePluginAdapter(PayloadPlugin)
object JsonSchemaParsePlugin           extends AMFParsePluginAdapter(JsonSchemaPlugin)
object ExternalJsonYamlRefsParsePlugin extends AMFParsePluginAdapter(ExternalJsonYamlRefsPlugin)
