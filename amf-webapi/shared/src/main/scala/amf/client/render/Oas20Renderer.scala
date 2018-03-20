package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.OAS20Plugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * Oas generator.
  */
@JSExportAll
class Oas20Renderer extends Renderer("OAS 2.0", "application/json") {
  AMFPluginsRegistry.registerDocumentPlugin(OAS20Plugin)
}
