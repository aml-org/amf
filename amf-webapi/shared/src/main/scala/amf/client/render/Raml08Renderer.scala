package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.RAML08Plugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * Raml generator.
  */
@JSExportAll
class Raml08Renderer extends Renderer("RAML 0.8", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
}
