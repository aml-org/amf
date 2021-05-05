package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages._
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.{AmfProfile, ProfileName}

class AmfResolutionPipeline private[amf] (override val name: String) extends ResolutionPipeline() {
  def profileName: ProfileName = AmfProfile

  protected def references = new WebApiReferenceResolutionStage(keepEditingInfo = false)

  protected def parameterNormalizationStage: ParametersNormalizationStage =
    new AmfParametersNormalizationStage()

  override def steps: Seq[ResolutionStage] =
    Seq(
      references,
      new ExternalSourceRemovalStage,
      new ExtensionsResolutionStage(profileName, keepEditingInfo = false),
      new ShapeNormalizationStage(profileName, keepEditingInfo = false),
      new SecurityResolutionStage(),
      parameterNormalizationStage,
      new ServersNormalizationStage(profileName),
      new PathDescriptionNormalizationStage(profileName),
      new MediaTypeResolutionStage(profileName),
      new ResponseExamplesResolutionStage(),
      new PayloadAndParameterResolutionStage(profileName),
      new CleanReferencesStage(),
      new DeclarationsRemovalStage(),
      new AnnotationRemovalStage()
    )
}

object AmfResolutionPipeline {
  val name: String = ResolutionPipeline.DEFAULT_PIPELINE
  def apply()      = new AmfResolutionPipeline(name)
}
