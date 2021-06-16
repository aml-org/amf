package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage._
import amf.apicontract.internal.transformation.stages.ExtensionsResolutionStage
import amf.core.client.common.transform._
import amf.core.client.common.validation.{AmfProfile, ProfileName}
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.client.scala.transform.stages.{
  CleanReferencesStage,
  DeclarationsRemovalStage,
  ExternalSourceRemovalStage,
  TransformationStep
}
import amf.shapes.internal.domain.resolution.ShapeNormalizationStage

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
  val name: String = PipelineId.Default
  def apply()      = new AmfTransformationPipeline(name)
}
