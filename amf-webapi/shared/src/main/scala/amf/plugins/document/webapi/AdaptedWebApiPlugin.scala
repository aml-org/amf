package amf.plugins.document.webapi

import amf.client.`new`.amfcore.plugins.AmfParsePluginAdapter

object Async20ParsePlugin              extends AmfParsePluginAdapter(Async20Plugin)
object Oas20ParsePlugin                extends AmfParsePluginAdapter(Oas20Plugin)
object Oas30ParsePlugin                extends AmfParsePluginAdapter(Oas30Plugin)
object Raml08ParsePlugin               extends AmfParsePluginAdapter(Raml08Plugin)
object Raml10ParsePlugin               extends AmfParsePluginAdapter(Raml10Plugin)
object PayloadParsePlugin              extends AmfParsePluginAdapter(PayloadPlugin)
object JsonSchemaParsePlugin           extends AmfParsePluginAdapter(JsonSchemaPlugin)
object ExternalJsonYamlRefsParsePlugin extends AmfParsePluginAdapter(ExternalJsonYamlRefsPlugin)
