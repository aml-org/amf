package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.resolution.pipelines.TransformationPipeline.EDITING_PIPELINE
import amf.core.resolution.stages.{TransformationStep, UrlShortenerStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.{AmfProfile, ProfileName}

class AmfEditingPipeline private[amf] (urlShortening: Boolean = true, override val name: String)
    extends TransformationPipeline() {

  protected def references = new WebApiReferenceResolutionStage(keepEditingInfo = true)
  protected def url: Option[UrlShortenerStage] =
    if (urlShortening) Some(new UrlShortenerStage()) else None

  protected def parameterNormalizationStage: ParametersNormalizationStage =
    new AmfParametersNormalizationStage()

  override def steps: Seq[TransformationStep] = {
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

object AmfEditingPipeline {
  val name: String = TransformationPipeline.EDITING_PIPELINE
  def apply()      = new AmfEditingPipeline(name = name)
}
