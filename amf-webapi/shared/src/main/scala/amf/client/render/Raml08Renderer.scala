package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Raml08
import amf.plugins.document.webapi.Raml08Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Raml generator.
  */
@JSExportTopLevel("Raml08Renderer")
@JSExportAll
class Raml08Renderer extends Renderer(Raml08.name, "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
}
