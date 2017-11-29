package amf.core.services

import amf.core.model.document.BaseUnit
import amf.core.plugins.AMFDocumentPlugin
import amf.core.registries.AMFPluginsRegistry

object RuntimeResolver {

  def resolve(vendor: String, unit: BaseUnit): BaseUnit = {
    var plugin = AMFPluginsRegistry.documentPluginForID(vendor) match {
      case Some(documentPlugin) => documentPlugin
      case None => AMFPluginsRegistry.documentPluginForVendor(vendor).headOption
    }

    plugin match {
      case Some(documentPlugin: AMFDocumentPlugin) => documentPlugin.resolve(unit)
      case None                                    => throw new Exception(s"Cannot find domain plugin for vendor $vendor to resolve unit ${unit.location}")
    }
  }
}
