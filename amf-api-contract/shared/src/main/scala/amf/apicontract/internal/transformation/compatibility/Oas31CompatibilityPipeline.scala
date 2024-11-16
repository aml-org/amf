package amf.apicontract.internal.transformation.compatibility

import amf.apicontract.internal.transformation.Oas31TransformationPipeline
import amf.core.client.common.transform._
import amf.core.client.scala.transform.TransformationStep

class Oas31CompatibilityPipeline private[amf] (override val name: String) extends Oas3CompatibilityPipeline(name) {

  override val baseSteps: Seq[TransformationStep] = filterOutSemanticStage(Oas31TransformationPipeline().steps)

  override def steps: Seq[TransformationStep] = super.steps
}

object Oas31CompatibilityPipeline {
  def apply(): Oas31CompatibilityPipeline = new Oas31CompatibilityPipeline(name)
  val name: String                        = PipelineId.Compatibility
}
