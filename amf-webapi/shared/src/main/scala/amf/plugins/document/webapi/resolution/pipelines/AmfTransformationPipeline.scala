package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.resolution.stages._
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages._
import amf.{AmfProfile, ProfileName}

class AmfTransformationPipeline private[amf] (override val name: String) extends TransformationPipeline() {
  def profileName: ProfileName = AmfProfile

  protected def references = new WebApiReferenceResolutionStage(keepEditingInfo = false)

  protected def parameterNormalizationStage: ParametersNormalizationStage =
    new AmfParametersNormalizationStage()

  override def steps: Seq[TransformationStep] =
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

object AmfTransformationPipeline {
  val name: String = TransformationPipeline.DEFAULT_PIPELINE
  def apply()      = new AmfTransformationPipeline(name)
}
