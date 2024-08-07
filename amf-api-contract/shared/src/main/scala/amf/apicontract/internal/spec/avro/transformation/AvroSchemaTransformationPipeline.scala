package amf.apicontract.internal.spec.avro.transformation

import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.AvroSchemaProfile
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{ReferenceResolutionStage, SourceInformationStage}
import amf.shapes.internal.domain.resolution.ShapeNormalizationForUnitStage

class AvroSchemaTransformationPipeline private (override val name: String) extends TransformationPipeline() {
  def references = new ReferenceResolutionStage(false)

  override def steps: Seq[TransformationStep] =
    Seq(
      references,
      new ShapeNormalizationForUnitStage(AvroSchemaProfile, keepEditingInfo = false),
      SourceInformationStage
    )
}

object AvroSchemaTransformationPipeline {
  def apply()      = new AvroSchemaTransformationPipeline(name)
  val name: String = PipelineId.Default
}
