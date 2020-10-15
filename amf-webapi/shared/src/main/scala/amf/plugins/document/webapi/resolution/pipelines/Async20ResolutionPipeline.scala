package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
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
import amf.{Async20Profile, ProfileName}

class Async20ResolutionPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {
  override def profileName: ProfileName = Async20Profile
  def references                        = new WebApiReferenceResolutionStage()

  override val steps: Seq[ResolutionStage] = Seq(
    references,
    new ExternalSourceRemovalStage,
    new ShapeNormalizationStage(profileName, keepEditingInfo = false),
    new JsonMergePatchStage(),
    new AsyncContentTypeResolutionStage(),
    new AsyncExamplePropagationResolutionStage(),
    new ServerVariableExampleResolutionStage(),
    new PathDescriptionNormalizationStage(profileName),
    new CleanReferencesStage(),
    new DeclarationsRemovalStage(),
    new AnnotationRemovalStage()
  )
}
