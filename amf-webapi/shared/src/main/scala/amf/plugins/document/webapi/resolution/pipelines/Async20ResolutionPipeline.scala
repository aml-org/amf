package amf.plugins.document.webapi.resolution.pipelines

import amf.Async20Profile
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.AsyncApi20
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{
  CleanReferencesStage,
  DeclarationsRemovalStage,
  ExternalSourceRemovalStage,
  ResolutionStage
}
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.plugins.domain.webapi.resolution.stages.async.{
  AsyncContentTypeResolutionStage,
  AsyncExamplePropagationResolutionStage,
  ServerVariableExampleResolutionStage
}

class Async20ResolutionPipeline private (override val name: String) extends ResolutionPipeline() {
  def references = new WebApiReferenceResolutionStage()

  override def steps: Seq[ResolutionStage] =
    Seq(
      references,
      new ExternalSourceRemovalStage,
      new ShapeNormalizationStage(Async20Profile, keepEditingInfo = false),
      new JsonMergePatchStage(isEditing = false),
      new AsyncContentTypeResolutionStage(),
      new AsyncExamplePropagationResolutionStage(),
      new ServerVariableExampleResolutionStage(),
      new PathDescriptionNormalizationStage(Async20Profile),
      new CleanReferencesStage(),
      new DeclarationsRemovalStage(),
      new AnnotationRemovalStage()
    )
}

object Async20ResolutionPipeline {
  def apply()      = new Async20ResolutionPipeline(name)
  val name: String = PipelineName.from(AsyncApi20.name, ResolutionPipeline.DEFAULT_PIPELINE)
}
