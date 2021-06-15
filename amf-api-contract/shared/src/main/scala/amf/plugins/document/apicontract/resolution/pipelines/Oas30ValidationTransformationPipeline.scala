package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.Oas30Profile
import amf.core.client.scala.transform.stages.TransformationStep
import amf.shapes.internal.domain.resolution.RequestParamsLinkStage

class Oas30ValidationTransformationPipeline private (override val name: String)
    extends ValidationTransformationPipeline(Oas30Profile, name) {

  override def steps: Seq[TransformationStep] = RequestParamsLinkStage +: super.steps
}

object Oas30ValidationTransformationPipeline {
  val name: String = "Oas30ValidationTransformationPipeline"
  def apply()      = new Oas30ValidationTransformationPipeline(name)
}
