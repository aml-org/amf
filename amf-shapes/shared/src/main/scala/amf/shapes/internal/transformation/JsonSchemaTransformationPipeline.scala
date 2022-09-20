package amf.shapes.internal.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.JsonSchemaProfile
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{CleanReferencesStage, ExternalSourceRemovalStage, ReferenceResolutionStage, SourceInformationStage}
import amf.shapes.internal.domain.resolution.{ShapeNormalizationStage, ShapeNormalizationStage2}

class JsonSchemaTransformationPipeline private(override val name: String) extends TransformationPipeline() {
  def references = new ReferenceResolutionStage(false)

  override def steps: Seq[TransformationStep] =
    Seq(
      references,
      new ExternalSourceRemovalStage,
      new ShapeNormalizationStage2(JsonSchemaProfile, keepEditingInfo = false),
      new CleanReferencesStage(),
      SourceInformationStage
    )
}

object JsonSchemaTransformationPipeline {
  def apply()      = new JsonSchemaTransformationPipeline(name)
  val name: String = PipelineId.Default
}
