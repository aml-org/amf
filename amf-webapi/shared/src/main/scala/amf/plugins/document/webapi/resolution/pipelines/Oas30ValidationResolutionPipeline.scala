package amf.plugins.document.webapi.resolution.pipelines

import amf.Oas30Profile
import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage

class Oas30ValidationResolutionPipeline(override val eh: ErrorHandler)
    extends ValidationResolutionPipeline(Oas30Profile, eh) {

  override val steps: Seq[ResolutionStage] = new RequestParamsLinkStage() +: baseSteps
}
