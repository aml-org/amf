package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{Async20Profile, ProfileName}
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.client.common.transform._
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.remote.AsyncApi20
import amf.plugins.domain.apicontract.resolution.stages._
import amf.plugins.domain.apicontract.resolution.stages.async.{
  AsyncContentTypeResolutionStage,
  AsyncExamplePropagationResolutionStage,
  ServerVariableExampleResolutionStage
}
import amf.shapes.internal.domain.resolution.ShapeNormalizationStage

class Async20EditingPipeline private (urlShortening: Boolean = true, override val name: String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Async20Profile

  override def references = new WebApiReferenceResolutionStage(true)

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override def steps: Seq[TransformationStep] =
    Seq(
      references,
      new ShapeNormalizationStage(profileName, keepEditingInfo = true),
      new JsonMergePatchStage(isEditing = true),
      new AsyncContentTypeResolutionStage(),
      new AsyncExamplePropagationResolutionStage(),
      new ServerVariableExampleResolutionStage(),
      new PathDescriptionNormalizationStage(profileName, keepEditingInfo = true),
      new AnnotationRemovalStage()
    ) ++ url
}

object Async20EditingPipeline {
  def apply()                    = new Async20EditingPipeline(true, name)
  private[amf] def cachePipeline = new Async20EditingPipeline(false, Async20CachePipeline.name)
  val name: String               = PipelineName.from(AsyncApi20.mediaType, PipelineId.Editing)
}

object Async20CachePipeline {
  val name: String                                 = PipelineName.from(AsyncApi20.mediaType, PipelineId.Cache)
  private[amf] def apply(): Async20EditingPipeline = Async20EditingPipeline.cachePipeline
}
