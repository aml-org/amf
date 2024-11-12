package amf.apicontract.internal.transformation

import amf.core.client.common.validation.{Oas31Profile, ProfileName}
import amf.core.client.scala.transform.TransformationStep

class Oas31ValidationTransformationPipeline(override val name: String, val profileName: ProfileName)
    extends Oas30ValidationTransformationPipeline(name, profileName) {

  override def steps: Seq[TransformationStep] = super.steps
}

object Oas31ValidationTransformationPipeline {
  val name: String = "Oas31ValidationTransformationPipeline"
  def apply()      = new Oas31ValidationTransformationPipeline(name, Oas31Profile)
}
