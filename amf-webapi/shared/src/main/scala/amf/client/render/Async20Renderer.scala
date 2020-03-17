package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.AsyncApi20
import amf.plugins.document.webapi.Async20Plugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Oas generator.
  */
@JSExportAll
@JSExportTopLevel("Async20Renderer")
class Async20Renderer extends Renderer(AsyncApi20.name, "application/yaml") {
  AMFPluginsRegistry.registerDocumentPlugin(Async20Plugin)
}
