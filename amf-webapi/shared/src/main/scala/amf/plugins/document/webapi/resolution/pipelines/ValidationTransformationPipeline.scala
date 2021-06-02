package amf.plugins.document.webapi.resolution.pipelines

import amf.client.remod.AMFGraphConfiguration
import amf.{Async20Profile, Oas30Profile, ProfileName}
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.{TransformationPipeline, TransformationPipelineRunner}
import amf.core.resolution.stages.{ExternalSourceRemovalStage, ReferenceResolutionStage, TransformationStep}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{
  AnnotationRemovalStage,
  MediaTypeResolutionStage,
  PayloadAndParameterResolutionStage,
  ResponseExamplesResolutionStage
}

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
