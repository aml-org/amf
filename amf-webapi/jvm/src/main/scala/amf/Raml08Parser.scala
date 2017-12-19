package amf

import amf.core.client.Parser
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml
import amf.plugins.document.webapi.RAML08Plugin

/**
  * [[Raml]] parser.
  */
class Raml08Parser extends Parser("RAML 0.8", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
}
