package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileName
import amf.core.annotations.LexicalInformation
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

class ValidationShapeNormalisationStage(profile: ProfileName, override val keepEditingInfo: Boolean)(
    override implicit val errorHandler: ErrorHandler)
    extends ShapeNormalizationStage(profile, keepEditingInfo) {

  override protected val context: NormalizationContext = HandledNormalizationContext()

  case class HandledNormalizationContext() extends NormalizationContext(errorHandler, keepEditingInfo, profile) {

    override def minShape(base: Shape, superShape: Shape): Shape = {
      try {
        super.minShape(base, superShape)
      } catch {
        case e: InheritanceIncompatibleShapeError =>
          errorHandler.violation(
            ParserSideValidations.InvalidTypeInheritanceErrorSpecification.id,
            base.id,
            Some(ShapeModel.Inherits.value.iri()),
            e.getMessage,
            base.annotations.find(classOf[LexicalInformation])
          )
          base
        case other: Exception => throw other
      }
    }
  }
}

class ValidationResolutionPipeline(profile: ProfileName, override val model: BaseUnit)
    extends ResolutionPipeline[BaseUnit] {

  override protected val steps: Seq[ResolutionStage] = Seq(
    new ReferenceResolutionStage(keepEditingInfo = false),
    new ExtensionsResolutionStage(profile, keepEditingInfo = false),
    new ValidationShapeNormalisationStage(profile, keepEditingInfo = false)
  )

  override def profileName: ProfileName = profile
}
