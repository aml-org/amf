package amf

import amf.core.client.Parser
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Amf
import amf.plugins.document.graph.AMFGraphPlugin

/**
  * [[Amf]] parser.
  */
class AmfGraphParser extends Parser("AMF Graph", "application/ld+json") {
  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
}
