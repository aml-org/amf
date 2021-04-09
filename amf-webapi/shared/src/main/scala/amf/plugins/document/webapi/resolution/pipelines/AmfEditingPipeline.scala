package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.pipelines.ResolutionPipeline.EDITING_PIPELINE
import amf.core.resolution.stages.{ResolutionStage, UrlShortenerStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.{AmfProfile, ProfileName}

class AmfEditingPipeline(urlShortening: Boolean = true) extends ResolutionPipeline() {

  protected def references(implicit eh: ErrorHandler) = new WebApiReferenceResolutionStage(keepEditingInfo = true)
  protected def url(implicit eh: ErrorHandler): Option[UrlShortenerStage] =
    if (urlShortening) Some(new UrlShortenerStage()) else None

  protected def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new AmfParametersNormalizationStage()

  override def steps(implicit eh: ErrorHandler): Seq[ResolutionStage] = {
    Seq(
      references,
      new ExtensionsResolutionStage(profileName, keepEditingInfo = true),
      new ShapeNormalizationStage(profileName, keepEditingInfo = true),
      new SecurityResolutionStage(),
      parameterNormalizationStage,
      new ServersNormalizationStage(profileName, keepEditingInfo = true),
      new PathDescriptionNormalizationStage(profileName, keepEditingInfo = true),
      new MediaTypeResolutionStage(profileName, keepEditingInfo = true),
      new ResponseExamplesResolutionStage(),
      new PayloadAndParameterResolutionStage(profileName),
      new AnnotationRemovalStage()
    ) ++ url
  }

  val ID: String               = EDITING_PIPELINE
  def profileName: ProfileName = AmfProfile
}
