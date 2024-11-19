package amf.apicontract.internal.transformation

import amf.core.client.common.transform._
import amf.core.client.common.validation.{Oas31Profile, ProfileName}
import amf.core.client.scala.transform.TransformationStep

class Oas31TransformationPipeline private[amf] (override val name: String) extends Oas30TransformationPipeline(name) {
  override def profileName: ProfileName = Oas31Profile

  override def steps: Seq[TransformationStep] = super.steps
}

object Oas31TransformationPipeline {
  def apply()      = new Oas31TransformationPipeline(name)
  val name: String = PipelineId.Default
}
