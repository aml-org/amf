package amf.mcp.internal.transformation

import amf.core.client.common.transform._
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.UrlShortenerStage

class MCPEditingPipeline private (urlShortening: Boolean = true, val name: String) extends TransformationPipeline {

  private def url: Option[UrlShortenerStage] = if (urlShortening) Some(new UrlShortenerStage()) else None

  override def steps: Seq[TransformationStep] = Nil
}

object MCPEditingPipeline {
  def apply()                    = new MCPEditingPipeline(true, name)
  private[amf] def cachePipeline = new MCPEditingPipeline(false, MCPCachePipeline.name)
  val name: String               = PipelineId.Editing
}

object MCPCachePipeline {
  val name: String                             = PipelineId.Cache
  private[amf] def apply(): MCPEditingPipeline = MCPEditingPipeline.cachePipeline
}
