package amf.plugins.document.webapi.resolution.pipelines

import amf.Oas30Profile
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage

class Oas30ValidationResolutionPipeline() extends ValidationResolutionPipeline(Oas30Profile) {

  override def steps(model: BaseUnit, sourceVendor: String)(
      implicit errorHandler: ErrorHandler): Seq[ResolutionStage] =
    new RequestParamsLinkStage() +: super.steps(model, sourceVendor)
}
