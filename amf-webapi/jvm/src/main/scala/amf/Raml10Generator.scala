package amf

import amf.core.client.Generator
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml
import amf.plugins.document.webapi.RAML10Plugin

/**
  * [[Raml]] generator.
  */
class Raml10Generator extends Generator("RAML 1.0", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
}
