package amf.plugins.document.webapi.resolution.pipelines

import amf.Oas30Profile
import amf.core.errorhandling.AMFErrorHandler
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage

class Oas30ValidationTransformationPipeline private (override val name: String)
    extends ValidationTransformationPipeline(Oas30Profile, name) {

  override def steps: Seq[TransformationStep] = RequestParamsLinkStage +: super.steps
}

object Oas30ValidationTransformationPipeline {
  val name: String = "Oas30ValidationTransformationPipeline"
  def apply()      = new Oas30ValidationTransformationPipeline(name)
}
