package amf.shapes.internal.domain.resolution.shape_normalization2

import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.internal.validation.definitions.ShapeResolutionSideValidations.InvalidTypeInheritanceWarningSpecification

private[resolution] class NormalizationContext2(
    final val errorHandler: AMFErrorHandler,
    final val keepEditingInfo: Boolean,
    final val profile: ProfileName,
    val resolvedInheritanceCache: NormalizationCache2 = NormalizationCache2(),
    val fixedReferencesCache: NormalizationCache2 = NormalizationCache2()
) {

  val isRaml08: Boolean                         = profile.equals(Raml08Profile)
  private val minShapeClass: MinShapeAlgorithm2 = new MinShapeAlgorithm2()(this)

  def minShape(derivedShape: Shape, superShape: Shape): Shape = {

    try {
      minShapeClass.computeMinShape(derivedShape, superShape)
    } catch {
      case e: InheritanceIncompatibleShapeError =>
        errorHandler.violation(
          InvalidTypeInheritanceWarningSpecification,
          derivedShape.id,
          e.property.orElse(Some(ShapeModel.Inherits.value.iri())),
          e.getMessage,
          e.position,
          e.location
        )
        derivedShape
      case other: Throwable =>
        errorHandler.violation(
          TransformationValidation,
          derivedShape.id,
          Some(ShapeModel.Inherits.value.iri()),
          other.getMessage,
          derivedShape.position(),
          derivedShape.location()
        )
        derivedShape
    }
  }

}
