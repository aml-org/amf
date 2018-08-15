package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml10
import amf.plugins.document.webapi.Raml10Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Raml generator.
  */
@JSExportAll
@JSExportTopLevel("Raml10Renderer")
class Raml10Renderer extends Renderer(Raml10.name, "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
}
