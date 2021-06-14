package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{AmfProfile, ProfileName}
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.client.scala.transform.stages.{
  CleanReferencesStage,
  DeclarationsRemovalStage,
  ExternalSourceRemovalStage,
  TransformationStep
}
import amf.plugins.document.apicontract.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.apicontract.resolution.stages._
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage

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
