package amf.shapes.internal.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{CleanReferencesStage, ExternalSourceRemovalStage, SourceInformationStage}

class JsonSchemaBasedSpecTransformationPipeline private (override val name: String) extends TransformationPipeline() {
  override def steps: Seq[TransformationStep] =
    Seq(new ExternalSourceRemovalStage, new CleanReferencesStage, SourceInformationStage)
}

object JsonSchemaBasedSpecTransformationPipeline {
  def apply()      = new JsonSchemaBasedSpecTransformationPipeline(name)
  val name: String = PipelineId.Default
}
