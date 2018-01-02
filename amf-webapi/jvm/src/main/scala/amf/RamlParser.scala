package amf

import amf.core.client.Parser
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml
import amf.plugins.document.webapi.{RAML08Plugin, RAML10Plugin}

/**
  * [[Raml]] parser.
  */
class RamlParser extends Parser("RAML", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
  AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
}
