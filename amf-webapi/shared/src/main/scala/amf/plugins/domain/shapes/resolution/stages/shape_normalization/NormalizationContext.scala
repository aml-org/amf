package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.plugins.domain.shapes.resolution.stages.shape_normalization.RecursionPropagation.REJECT_ALL
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import amf.validations.ResolutionSideValidations.InvalidTypeInheritanceWarningSpecification
import amf.{ProfileName, Raml08Profile}

private[plugins] class NormalizationContext(final val errorHandler: ErrorHandler,
                                            final val keepEditingInfo: Boolean,
                                            final val profile: ProfileName,
                                            val cache: NormalizationCache = NormalizationCache())
    extends ClosureHelper {

  val isRaml08: Boolean                        = profile.equals(Raml08Profile)
  private val minShapeClass: MinShapeAlgorithm = new MinShapeAlgorithm()(this)

  override protected def addClosure(closure: Shape, target: Shape): Unit = {
    super.addClosure(closure, target)
    cache.cacheClosure(closure.id, target)
    updateAssociatedShapes(closure, target)
  }

  /*
   * search shapes that have target as a closure, and add the new closure that was added to target
   */
  def updateAssociatedShapes(closure: Shape, target: Shape): Unit =
    cache.cacheWithClosures(target.id) match {
      case Some(seq) =>
        // shape has target as a closure, so the new closure of target should also be added
        seq.foreach { shape =>
          if (!shape.closureShapes.contains(closure)) addClosure(closure, shape)
        }
      case _ =>
    }

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
