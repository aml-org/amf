package amf.shapes.internal.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}

class JsonSchemaBasedSpecTransformationPipeline private (override val name: String) extends TransformationPipeline() {
  override def steps: Seq[TransformationStep] = Nil
}

object JsonSchemaBasedSpecTransformationPipeline {
  def apply()      = new JsonSchemaBasedSpecTransformationPipeline(name)
  val name: String = PipelineId.Default
}
