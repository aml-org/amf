package amf.plugins.document.webapi.resolution.pipelines

import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.AsyncApi20
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.plugins.domain.webapi.resolution.stages.async.{
  AsyncContentTypeResolutionStage,
  AsyncExamplePropagationResolutionStage,
  ServerVariableExampleResolutionStage
}
import amf.{Async20Profile, ProfileName}

class Async20EditingPipeline private (urlShortening: Boolean = true, override val name: String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Async20Profile

  override def references = new WebApiReferenceResolutionStage(true)

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override def steps: Seq[ResolutionStage] =
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
  val name: String               = PipelineName.from(AsyncApi20.name, ResolutionPipeline.EDITING_PIPELINE)
}

object Async20CachePipeline {
  val name: String                                 = PipelineName.from(AsyncApi20.name, ResolutionPipeline.CACHE_PIPELINE)
  private[amf] def apply(): Async20EditingPipeline = Async20EditingPipeline.cachePipeline
}
