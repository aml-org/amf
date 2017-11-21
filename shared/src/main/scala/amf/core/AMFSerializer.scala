package amf.core

import amf.client.GenerationOptions
import amf.document.BaseUnit
import amf.plugins.domain.graph.AMFGraphPlugin
import amf.plugins.domain.payload.PayloadPlugin
import amf.plugins.domain.vocabularies.RAMLExtensionsPlugin
import amf.plugins.domain.webapi.{OAS20Plugin, RAML10Plugin}
import amf.plugins.syntax.SYamlSyntaxPlugin
import org.yaml.model.YDocument

class AMFSerializer {

  // temporary
  AMFPluginsRegistry.registerSyntaxPlugin(new SYamlSyntaxPlugin())
  AMFPluginsRegistry.registerDomainPlugin(new AMFGraphPlugin())
  AMFPluginsRegistry.registerDomainPlugin(new PayloadPlugin())
  AMFPluginsRegistry.registerDomainPlugin(new RAMLExtensionsPlugin())
  AMFPluginsRegistry.registerDomainPlugin(new OAS20Plugin())
  AMFPluginsRegistry.registerDomainPlugin(new RAML10Plugin())
  //

  def make(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions): YDocument = {
    val domainPluginOption = AMFPluginsRegistry.domainPluginForVendor(vendor).find { plugin =>
      plugin.domainSyntaxes.contains(mediaType) && plugin.canUnparse(unit)
    } match {
      case Some(domainPlugin) => Some(domainPlugin)
      case None => AMFPluginsRegistry.domainPluginForMediaType(mediaType).find(_.canUnparse(unit))
    }

    domainPluginOption match {
      case Some(domainPlugin) =>  domainPlugin.unparse(unit, options) match {
        case Some(ast) => ast
        case None      => throw new Exception(s"Error unparsing syntax $mediaType with domain plugin ${domainPlugin.ID}")
      }
      case None => throw new Exception(s"Cannot parse domain model for media type $mediaType and vendor $vendor")
    }
  }
}
