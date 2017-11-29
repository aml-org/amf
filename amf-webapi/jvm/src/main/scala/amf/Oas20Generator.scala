package amf

import amf.core.client.Generator
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas
import amf.plugins.document.webapi.OAS20Plugin

/**
  * [[Oas]] generator.
  */
class Oas20Generator extends Generator("OAS 2.0", "application/json") {
  AMFPluginsRegistry.registerDocumentPlugin(OAS20Plugin)
}
