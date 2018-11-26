package amf.core.services

import amf.core.model.document.BaseUnit
import amf.client.plugins.AMFDocumentPlugin
import amf.core.parser.ErrorHandler
import amf.core.registries.AMFPluginsRegistry
import amf.plugins.features.validation.ParserSideValidations

object RuntimeResolver {

  def resolve(vendor: String, unit: BaseUnit, pipelineId: String, errorHandler: ErrorHandler): BaseUnit = {
    var plugin = AMFPluginsRegistry.documentPluginForID(vendor) match {
      case Some(documentPlugin) => Some(documentPlugin)
      case None                 => AMFPluginsRegistry.documentPluginForVendor(vendor).headOption
    }

    plugin match {
      case Some(documentPlugin: AMFDocumentPlugin) => documentPlugin.resolve(unit, errorHandler, pipelineId)
      case None =>
        errorHandler.violation(
          ParserSideValidations.ResolutionErrorSpecification.id,
          s"Cannot find domain plugin for vendor $vendor to resolve unit ${unit.location}",
          unit.position(),
          unit.location()
        )
        unit
    }
  }
}
