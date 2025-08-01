package amf.grpc.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.AnnotationRemovalStage
import amf.core.client.common.transform._
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{ReferenceResolutionStage, SourceInformationStage, UrlShortenerStage}

class GrpcEditingPipeline private (urlShortening: Boolean = true, val name: String) extends TransformationPipeline {

  private def url: Option[UrlShortenerStage] = if (urlShortening) Some(new UrlShortenerStage()) else None

  override def steps: Seq[TransformationStep] = Seq(
    new ReferenceResolutionStage(keepEditingInfo = true),
    new AnnotationRemovalStage()
  ) ++ url :+ SourceInformationStage
}

object GrpcEditingPipeline {
  def apply()                    = new GrpcEditingPipeline(true, name)
  private[amf] def cachePipeline = new GrpcEditingPipeline(false, GrpcCachePipeline.name)
  val name: String               = PipelineId.Editing
}

object GrpcCachePipeline {
  val name: String                              = PipelineId.Cache
  private[amf] def apply(): GrpcEditingPipeline = GrpcEditingPipeline.cachePipeline
}
