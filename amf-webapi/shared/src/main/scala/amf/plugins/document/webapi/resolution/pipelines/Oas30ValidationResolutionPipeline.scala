package amf.plugins.document.webapi.resolution.pipelines

import amf.Oas30Profile
import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage

class Oas30ValidationResolutionPipeline() extends ValidationResolutionPipeline(Oas30Profile) {

  override def steps(implicit eh: ErrorHandler): Seq[ResolutionStage] = new RequestParamsLinkStage() +: super.steps(eh)
}
