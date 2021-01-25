package amf.client.`new`

import amf.ProfileName
import amf.client.`new`.amfcore.{AmfParsePlugin, AmfResolutionPipeline, AmfResolvePlugin, AmfValidatePlugin}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.DomainElement
import amf.core.remote.Vendor
import org.yaml.model.YDocument

case class AmfRegistry(plugins: PluginsRegistry,
                       entitiesRegistry: EntitiesRegistry,
                       resolutionPipelines: Map[ProfileName, AmfResolutionPipeline]) {}

// maps or just lists?
case class PluginsRegistry private[amf] (parsePlugins: Map[Vendor, AmfParsePlugin],
                                         resolvePlugins: Map[Vendor, AmfResolvePlugin],
                                         validatePlugins: Map[Vendor, AmfValidatePlugin],
                                         defaultPlugin: AmfParsePlugin) { // ?? default handling?){

  def getParsePluginFor(ast: YDocument, vendor: Vendor): AmfParsePlugin = {
    parsePlugins.filter(_._2.apply(ast, vendor)).toList match {
      case Nil         => defaultPlugin
      case head :: Nil => head._2
      case multiple    => multiple.min._2
    }
  }

  def getResolvePluginFor(bu: BaseUnit, vendor: Vendor): Option[AmfResolvePlugin]

  def getValidationsPlugin(bu: BaseUnit): Seq[AmfValidatePlugin]

  def getValidationPlugin(bu: BaseUnit, profile: ProfileName): Option[AmfValidatePlugin]
}

case class EntitiesRegistry(domainEntities: Map[String, DomainElement], wrappersRegistry: Map[String, DomainElement]) {}
