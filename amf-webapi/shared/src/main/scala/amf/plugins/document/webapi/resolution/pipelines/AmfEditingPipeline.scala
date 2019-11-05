package amf.plugins.document.webapi.resolution.pipelines

import amf.core.parser.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.pipelines.ResolutionPipeline.EDITING_PIPELINE
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolutionStage, UrlShortenerStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{
  ExamplesResolutionStage,
  MediaTypeResolutionStage,
  ParametersNormalizationStage,
  PathDescriptionNormalizationStage,
  SecurityResolutionStage,
  ServersNormalizationStage
}
import amf.{AmfProfile, ProfileName}

class AmfEditingPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  protected def references = new ReferenceResolutionStage(keepEditingInfo = true)

  override lazy val steps: Seq[ResolutionStage] = Seq(
    references,
    new ExtensionsResolutionStage(profileName, keepEditingInfo = true),
    new ShapeNormalizationStage(profileName, keepEditingInfo = true),
    new SecurityResolutionStage(),
    new ParametersNormalizationStage(profileName),
    new ServersNormalizationStage(profileName),
    new PathDescriptionNormalizationStage(profileName, keepEditingInfo = true),
    new MediaTypeResolutionStage(profileName, keepEditingInfo = true),
    new ExamplesResolutionStage(),
    new UrlShortenerStage()
  )

  val ID: String                        = EDITING_PIPELINE
  override def profileName: ProfileName = AmfProfile
}

object AmfEditingPipeline {
  def unhandled = new AmfEditingPipeline(UnhandledErrorHandler)
}
