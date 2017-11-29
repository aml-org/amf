package amf.core

import amf.core.registries.AMFPluginsRegistry
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.features.validation.ParserSideValidationPlugin

object AMF {
  def init(): Unit = {
    AMFCompiler.init()
    AMFSerializer.init()
    AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
    ParserSideValidationPlugin.init()
  }
}
