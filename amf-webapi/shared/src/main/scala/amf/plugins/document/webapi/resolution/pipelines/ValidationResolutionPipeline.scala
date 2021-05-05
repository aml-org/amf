package amf.plugins.document.webapi.resolution.pipelines

import amf.{Async20Profile, Oas30Profile, ProfileName}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{ExternalSourceRemovalStage, ReferenceResolutionStage, ResolutionStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{
  AnnotationRemovalStage,
  MediaTypeResolutionStage,
  PayloadAndParameterResolutionStage,
  ResponseExamplesResolutionStage
}

class ValidationResolutionPipeline private[amf] (profile: ProfileName,
                                                 override val name: String = "ValidationResolutionPipeline")
    extends ResolutionPipeline() {

  override def steps: Seq[ResolutionStage] =
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

object ValidationResolutionPipeline {
  def apply(profile: ProfileName, unit: BaseUnit): BaseUnit = {
    val pipeline = profile match {
      case Oas30Profile   => Oas30ValidationResolutionPipeline()
      case Async20Profile => Async20CachePipeline()
      case _              => new ValidationResolutionPipeline(profile)
    }
    pipeline.transform(unit, unit.errorHandler())
  }
}
