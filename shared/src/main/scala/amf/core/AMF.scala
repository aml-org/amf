package amf.core

import amf.framework.registries.AMFPluginsRegistry
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.RAMLExtensionsPlugin
import amf.plugins.document.webapi.{OAS20Plugin, PayloadPlugin, RAML10Plugin}
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.plugins.syntax.SYamlSyntaxPlugin

object AMF {
  def init(): Unit = {
    AMFCompiler.init()
    AMFSerializer.init()

    // temporary
    AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
    AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
    AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
    AMFPluginsRegistry.registerDocumentPlugin(RAMLExtensionsPlugin)
    AMFPluginsRegistry.registerDocumentPlugin(OAS20Plugin)
    AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
    AMFPluginsRegistry.registerDomainPlugin(WebAPIDomainPlugin)
    //

  }
}
