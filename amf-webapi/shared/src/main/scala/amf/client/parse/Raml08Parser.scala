package amf.client.parse

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.RAML08Plugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * Raml parser.
  */
@JSExportAll
class Raml08Parser extends Parser("RAML 0.8", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
}
