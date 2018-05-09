package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.graph.AMFGraphPlugin

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("JsonldRenderer")
class JsonldRenderer extends Renderer("AMF Graph", "application/ld+json") {
  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
}
