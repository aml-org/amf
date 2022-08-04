package amf.shapes.internal.transformation

import amf.core.client.common.transform._
import amf.core.client.common.validation.ProfileNames.JSONSCHEMA
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{ReferenceResolutionStage, SourceInformationStage, UrlShortenerStage}
import amf.shapes.internal.domain.resolution.ShapeNormalizationStage
import amf.shapes.internal.transformation.stages.ContextTransformationStage

class JsonSchemaEditingPipeline private (val name: String) extends TransformationPipeline {

  override def steps: Seq[TransformationStep] =
    Seq(
      new ReferenceResolutionStage(true),
      new ContextTransformationStage()
    ) :+ SourceInformationStage
}

object JsonSchemaEditingPipeline {
  def apply()                    = new JsonSchemaEditingPipeline(true, name)
  private[amf] def cachePipeline = new JsonSchemaEditingPipeline(false, PipelineId.Editing)
  val name: String               = PipelineId.Editing
}

object JsonSchemaCachePipeline {
  val name: String                                    = PipelineId.Cache
  private[amf] def apply(): JsonSchemaEditingPipeline = JsonSchemaEditingPipeline.cachePipeline
}
