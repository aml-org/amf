package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileName
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.ErrorHandler
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolutionStage}
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage
import amf.plugins.domain.shapes.resolution.stages.shape_normalization.{
  InheritanceIncompatibleShapeError,
  NormalizationContext
}
import amf.plugins.features.validation.ParserSideValidations

class ValidationResolutionPipeline(profile: ProfileName, override val model: BaseUnit)
    extends ResolutionPipeline[BaseUnit] {

  override protected val steps: Seq[ResolutionStage] = Seq(
    new ReferenceResolutionStage(keepEditingInfo = false),
    new ExtensionsResolutionStage(profile, keepEditingInfo = false),
    new ShapeNormalizationStage(profile, keepEditingInfo = false)
  )

  override def profileName: ProfileName = profile
}
