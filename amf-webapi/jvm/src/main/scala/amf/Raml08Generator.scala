package amf

import amf.core.client.Generator
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml
import amf.plugins.document.webapi.RAML08Plugin

/**
  * [[Raml]] generator.
  */
class Raml08Generator extends Generator("RAML 0.8", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
}
