package amf.core.client

import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.RuntimeResolver

abstract class PlatformResolver(vendor: String) {

  def resolve(unit: BaseUnit, errorHandler: ErrorHandler): BaseUnit =
    RuntimeResolver.resolve(vendor, unit, ResolutionPipeline.DEFAULT_PIPELINE, errorHandler)
  def resolve(unit: BaseUnit, pipelineId: String, errorHandler: ErrorHandler) =
    RuntimeResolver.resolve(vendor, unit, pipelineId, errorHandler)
}
