package amf.client.render

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.graph.AMFGraphPlugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * Amf generator.
  */
@JSExportAll
class AmfGraphRenderer extends Renderer("AMF Graph", "application/ld+json") {
  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
}
