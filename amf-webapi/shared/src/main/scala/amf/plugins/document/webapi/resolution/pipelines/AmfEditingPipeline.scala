package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.pipelines.ResolutionPipeline.EDITING_PIPELINE
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolutionStage, UrlShortenerStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.{AmfProfile, ProfileName}

class AmfEditingPipeline(override val eh: ErrorHandler, urlShortening: Boolean = true) extends ResolutionPipeline(eh) {

  protected def references                   = new ReferenceResolutionStage(keepEditingInfo = true)
  private def url: Option[UrlShortenerStage] = if (urlShortening) Some(new UrlShortenerStage()) else None

  override lazy val steps: Seq[ResolutionStage] = Seq(
    references,
    new ExtensionsResolutionStage(profileName, keepEditingInfo = true),
    new ShapeNormalizationStage(profileName, keepEditingInfo = true),
    new SecurityResolutionStage(),
    new ParametersNormalizationStage(profileName),
    new ServersNormalizationStage(profileName),
    new PathDescriptionNormalizationStage(profileName, keepEditingInfo = true),
    new MediaTypeResolutionStage(profileName, keepEditingInfo = true),
    new ResponseExamplesResolutionStage(),
    new PayloadAndParameterResolutionStage(profileName)
  ) ++ url

  val ID: String                        = EDITING_PIPELINE
  override def profileName: ProfileName = AmfProfile
}

object AmfEditingPipeline {
  def unhandled = new AmfEditingPipeline(UnhandledErrorHandler)
}
