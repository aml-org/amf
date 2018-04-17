package amf.client.parse

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.webapi.{RAML08Plugin, RAML10Plugin}

import scala.scalajs.js.annotation.JSExportAll

/**
  * Raml parser.
  */
@JSExportAll
class RamlParser extends Parser("RAML", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
  AMFPluginsRegistry.registerDocumentPlugin(RAML08Plugin)
}
