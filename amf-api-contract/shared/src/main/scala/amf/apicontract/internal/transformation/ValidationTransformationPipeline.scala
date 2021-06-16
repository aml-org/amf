package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.{AnnotationRemovalStage, MediaTypeResolutionStage, PayloadAndParameterResolutionStage, ResponseExamplesResolutionStage}
import amf.apicontract.internal.transformation.stages.ExtensionsResolutionStage
import amf.core.client.common.validation.{Async20Profile, Oas30Profile, ProfileName}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.pipelines.{TransformationPipeline, TransformationPipelineRunner}
import amf.core.client.scala.transform.stages.{ExternalSourceRemovalStage, ReferenceResolutionStage, TransformationStep}
import amf.plugins.domain.apicontract.resolution.stages.ResponseExamplesResolutionStage
import amf.shapes.internal.domain.resolution.ShapeNormalizationStage

// Validation pipeline is not registered in AMF configuration, is it only called internally.
class ValidationTransformationPipeline private[amf] (profile: ProfileName,
                                                     override val name: String = "ValidationTransformationPipeline")
    extends TransformationPipeline() {

  override def steps: Seq[TransformationStep] =
    Seq(
      new ReferenceResolutionStage(keepEditingInfo = false),
      new ExternalSourceRemovalStage,
      new ExtensionsResolutionStage(profile, keepEditingInfo = false),
      new ShapeNormalizationStage(profile, keepEditingInfo = false),
      new MediaTypeResolutionStage(profile, isValidation = true),
      new ResponseExamplesResolutionStage(),
      new PayloadAndParameterResolutionStage(profile),
      new AnnotationRemovalStage()
    )
}

object ValidationTransformationPipeline {
  def apply(profile: ProfileName, unit: BaseUnit, eh: AMFErrorHandler): BaseUnit = {
    val pipeline = profile match {
      case Oas30Profile   => Oas30ValidationTransformationPipeline()
      case Async20Profile => Async20CachePipeline()
      case _              => new ValidationTransformationPipeline(profile)
    }
    val runner = TransformationPipelineRunner(eh)
    runner.run(unit, pipeline)
  }
}
