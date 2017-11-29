package amf

import amf.core.client.Parser
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml
import amf.plugins.document.webapi.RAML10Plugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * [[Raml]] parser.
  */
@JSExportAll
class Raml10Parser extends Parser("RAML 1.0", "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
}
