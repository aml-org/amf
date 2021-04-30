package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import amf.validations.ShapeResolutionSideValidations.InvalidTypeInheritanceWarningSpecification
import amf.{ProfileName, Raml08Profile}

private[plugins] class NormalizationContext(final val errorHandler: ErrorHandler,
                                            final val keepEditingInfo: Boolean,
                                            final val profile: ProfileName,
                                            val cache: NormalizationCache = NormalizationCache()) {

  val isRaml08: Boolean                        = profile.equals(Raml08Profile)
  private val minShapeClass: MinShapeAlgorithm = new MinShapeAlgorithm()(this)

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
          ResolutionValidation,
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
