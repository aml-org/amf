package amf.plugins.document.webapi.resolution.pipelines

import amf.core.parser.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages._
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.{AmfProfile, ProfileName}

class AmfResolutionPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {
  override def profileName: ProfileName = AmfProfile

  protected def references = new ReferenceResolutionStage(keepEditingInfo = false)

  override val steps: Seq[ResolutionStage] = Seq(
    references,
    new ExternalSourceRemovalStage,
    new ExtensionsResolutionStage(profileName, keepEditingInfo = false),
    new ShapeNormalizationStage(profileName, keepEditingInfo = false),
    new SecurityResolutionStage(),
    new ParametersNormalizationStage(profileName),
    new ServersNormalizationStage(profileName),
    new PathDescriptionNormalizationStage(profileName),
    new MediaTypeResolutionStage(profileName),
    new ExamplesResolutionStage(),
    new CleanReferencesStage(),
    new DeclarationsRemovalStage()
  )

}

object AmfResolutionPipeline {
  def unhandled = new AmfResolutionPipeline(UnhandledErrorHandler)
}
