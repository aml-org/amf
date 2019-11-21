package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas30
import amf.plugins.document.webapi.Oas30Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Oas generator.
  */
@JSExportAll
@JSExportTopLevel("Oas30Renderer")
class Oas30Renderer extends Renderer(Oas30.name, "application/json") {
  AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
}
