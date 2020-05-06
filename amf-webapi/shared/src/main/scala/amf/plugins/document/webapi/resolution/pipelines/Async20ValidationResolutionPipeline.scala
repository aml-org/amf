package amf.plugins.document.webapi.resolution.pipelines

import amf.Async20Profile
import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.resolution.stages.async.ServerVariableExampleResolutionStage

class Async20ValidationResolutionPipeline(override val eh: ErrorHandler)
    extends ValidationResolutionPipeline(Async20Profile, eh) {

  override val steps: Seq[ResolutionStage] = baseSteps :+ new ServerVariableExampleResolutionStage()
}
