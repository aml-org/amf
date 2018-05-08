package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.RAML10Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Raml generator.
  */
@JSExportAll
class Raml10Renderer extends Renderer("RAML 1.0", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
}
