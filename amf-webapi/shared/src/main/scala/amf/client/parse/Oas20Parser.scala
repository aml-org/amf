package amf.client.parse

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.OAS20Plugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * Oas parser.
  */
@JSExportAll
class Oas20Parser extends Parser("OAS 2.0", "application/json") {
  AMFPluginsRegistry.registerDocumentPlugin(OAS20Plugin)
}
