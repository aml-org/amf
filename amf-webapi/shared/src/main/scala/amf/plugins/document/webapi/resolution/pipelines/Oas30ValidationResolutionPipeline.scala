package amf.plugins.document.webapi.resolution.pipelines

import amf.Oas30Profile
import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage

class Oas30ValidationResolutionPipeline private (override val name: String)
    extends ValidationResolutionPipeline(Oas30Profile, name) {

  override def steps(implicit eh: ErrorHandler): Seq[ResolutionStage] = new RequestParamsLinkStage() +: super.steps(eh)
}

object Oas30ValidationResolutionPipeline {
  val name: String = "Oas30ValidationResolutionPipeline"
  def apply()      = new Oas30ValidationResolutionPipeline(name)
}
