package amf.shapes.internal.transformation

import amf.core.client.common.transform._
import amf.core.client.common.validation.ProfileNames.JSONSCHEMA
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{ReferenceResolutionStage, SourceInformationStage, UrlShortenerStage}
import amf.shapes.internal.domain.resolution.ShapeNormalizationForUnitStage

class JsonSchemaEditingPipeline private (urlShortening: Boolean = true, val name: String)
    extends TransformationPipeline {

  private def url: Option[UrlShortenerStage] = if (urlShortening) Some(new UrlShortenerStage()) else None

  override def steps: Seq[TransformationStep] =
    Seq(
      new ReferenceResolutionStage(true),
      new ShapeNormalizationForUnitStage(JSONSCHEMA, keepEditingInfo = true)
    ) ++ url :+ SourceInformationStage
}

object JsonSchemaEditingPipeline {
  def apply()                    = new JsonSchemaEditingPipeline(true, name)
  private[amf] def cachePipeline = new JsonSchemaEditingPipeline(false, JsonSchemaCachePipeline.name)
  val name: String               = PipelineId.Editing
}

object JsonSchemaCachePipeline {
  val name: String                                    = PipelineId.Cache
  private[amf] def apply(): JsonSchemaEditingPipeline = JsonSchemaEditingPipeline.cachePipeline
}
