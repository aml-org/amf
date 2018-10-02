package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileName
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolutionStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.webapi.resolution.stages.MediaTypeResolutionStage

class ValidationResolutionPipeline(profile: ProfileName, override val model: BaseUnit)
    extends ResolutionPipeline[BaseUnit] {

  override protected val steps: Seq[ResolutionStage] = Seq(
    new ReferenceResolutionStage(keepEditingInfo = false),
    new ExtensionsResolutionStage(profile, keepEditingInfo = false),
    new ShapeNormalizationStage(profile, keepEditingInfo = false),
    new MediaTypeResolutionStage(profile, isValidation = true)
  )

  override def profileName: ProfileName = profile
}
