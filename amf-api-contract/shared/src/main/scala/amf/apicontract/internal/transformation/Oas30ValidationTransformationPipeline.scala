package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.RequestParamsLinkStage
import amf.core.client.common.validation.{Oas30Profile, ProfileName}
import amf.core.client.scala.transform.TransformationStep

class Oas30ValidationTransformationPipeline private[amf] (override val name: String, val profile: ProfileName)
    extends ValidationTransformationPipeline(profile, name) {

  override def steps: Seq[TransformationStep] = RequestParamsLinkStage +: super.steps
}

object Oas30ValidationTransformationPipeline {
  val name: String = "Oas30ValidationTransformationPipeline"
  def apply()      = new Oas30ValidationTransformationPipeline(name, Oas30Profile)
}
