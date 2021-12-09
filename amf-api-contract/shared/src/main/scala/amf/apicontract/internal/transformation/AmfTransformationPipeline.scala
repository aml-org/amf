package amf.apicontract.internal.transformation

import amf.aml.internal.transform.steps.SemanticExtensionFlatteningStage
import amf.apicontract.internal.spec.common.transformation.stage._
import amf.apicontract.internal.transformation.stages.{ExtensionsResolutionStage, WebApiReferenceResolutionStage}
import amf.core.client.common.transform._
import amf.core.client.common.validation.{AmfProfile, ProfileName}
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}
import amf.core.internal.transform.stages.{
  CleanReferencesStage,
  DeclarationsRemovalStage,
  ExternalSourceRemovalStage,
  SourceInformationStage
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
      new AnnotationRemovalStage(),
      SemanticExtensionFlatteningStage,
      SourceInformationStage
    )
}

object AmfTransformationPipeline {
  val name: String = PipelineId.Default
  def apply()      = new AmfTransformationPipeline(name)
}
