package amf.core.services

import amf.core.model.document.BaseUnit
import amf.client.plugins.AMFDocumentPlugin
import amf.core.registries.AMFPluginsRegistry

object RuntimeResolver {

  def resolve(vendor: String, unit: BaseUnit, pipelineId: String): BaseUnit = {
    var plugin = AMFPluginsRegistry.documentPluginForID(vendor) match {
      case Some(documentPlugin) => Some(documentPlugin)
      case None                 => AMFPluginsRegistry.documentPluginForVendor(vendor).headOption
    }

    plugin match {
      case Some(documentPlugin: AMFDocumentPlugin) => documentPlugin.resolve(unit, pipelineId)
      case None =>
        throw new Exception(s"Cannot find domain plugin for vendor $vendor to resolve unit ${unit.location}")
    }
  }
}
