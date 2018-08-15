package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Oas20
import amf.plugins.document.webapi.Oas20Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Oas generator.
  */
@JSExportAll
@JSExportTopLevel("Oas20Renderer")
class Oas20Renderer extends Renderer(Oas20.name, "application/json") {
  AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
}
