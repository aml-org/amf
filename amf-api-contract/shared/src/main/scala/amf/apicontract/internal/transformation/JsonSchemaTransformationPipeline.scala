package amf.apicontract.internal.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.Async20Profile
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{
  CleanReferencesStage,
  ExternalSourceRemovalStage,
  ReferenceResolutionStage,
  SourceInformationStage
}
import amf.shapes.internal.domain.resolution.ShapeNormalizationStage

class JsonSchemaTransformationPipeline private(override val name: String) extends TransformationPipeline() {
  def references = new ReferenceResolutionStage(false)

  override def steps: Seq[TransformationStep] =
    Seq(
      references,
      new ExternalSourceRemovalStage,
      new ShapeNormalizationStage(Async20Profile, keepEditingInfo = false),
      new CleanReferencesStage(),
      SourceInformationStage
    )
}

object JsonSchemaTransformationPipeline {
  def apply()      = new JsonSchemaTransformationPipeline(name)
  val name: String = PipelineId.Default
}
