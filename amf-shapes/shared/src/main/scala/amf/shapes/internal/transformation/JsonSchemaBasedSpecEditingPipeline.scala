package amf.shapes.internal.transformation

import amf.core.client.common.transform._
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{SourceInformationStage, UrlShortenerStage}

class JsonSchemaBasedSpecEditingPipeline private (urlShortening: Boolean = true, val name: String)
    extends TransformationPipeline {

  private def url: Option[UrlShortenerStage] = if (urlShortening) Some(new UrlShortenerStage()) else None

  override def steps: Seq[TransformationStep] = Seq() ++ url :+ SourceInformationStage
}

object JsonSchemaBasedSpecEditingPipeline {
  def apply()                    = new JsonSchemaBasedSpecEditingPipeline(true, name)
  private[amf] def cachePipeline = new JsonSchemaBasedSpecEditingPipeline(false, JsonSchemaBasedSpecCachePipeline.name)
  val name: String               = PipelineId.Editing
}

object JsonSchemaBasedSpecCachePipeline {
  val name: String                                             = PipelineId.Cache
  private[amf] def apply(): JsonSchemaBasedSpecEditingPipeline = JsonSchemaBasedSpecEditingPipeline.cachePipeline
}
