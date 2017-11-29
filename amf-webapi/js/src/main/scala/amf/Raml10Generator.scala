package amf

import amf.core.client.Generator
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml
import amf.plugins.document.webapi.RAML10Plugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * [[Raml]] generator.
  */
@JSExportAll
class Raml10Generator extends Generator("RAML 1.0", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
}
