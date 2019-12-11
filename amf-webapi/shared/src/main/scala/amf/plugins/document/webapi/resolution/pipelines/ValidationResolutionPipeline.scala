package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileName
import amf.core.model.document.BaseUnit
import amf.core.parser.{DefaultParserSideErrorHandler, ErrorHandler}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{ExternalSourceRemovalStage, ReferenceResolutionStage, ResolutionStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.{
  ResponseExamplesResolutionStage,
  PayloadAndParameterResolutionStage,
  MediaTypeResolutionStage
}

class ValidationResolutionPipeline(profile: ProfileName, override val eh: ErrorHandler)
    extends ResolutionPipeline(eh) {

  override val steps: Seq[ResolutionStage] = Seq(
    new ReferenceResolutionStage(keepEditingInfo = false),
    new ExternalSourceRemovalStage,
    new ExtensionsResolutionStage(profile, keepEditingInfo = false),
    new ShapeNormalizationStage(profile, keepEditingInfo = false),
    new MediaTypeResolutionStage(profile, isValidation = true),
    new ResponseExamplesResolutionStage(),
    new PayloadAndParameterResolutionStage(profile)
  )

  override def profileName: ProfileName = profile
}

object ValidationResolutionPipeline {
  def apply(profile: ProfileName, unit: BaseUnit): BaseUnit = {
    val handler = DefaultParserSideErrorHandler(unit)
    new ValidationResolutionPipeline(profile, handler).resolve(unit)
  }
}
