package amf.client.parse

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.graph.AMFGraphPlugin

import scala.scalajs.js.annotation.JSExportAll

/**
  * Amf parser.
  */
@JSExportAll
class AmfGraphParser extends Parser("AMF Graph", "application/ld+json") {
  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
}
