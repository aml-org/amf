package amf.plugins.domain.shapes.resolution.stages.recursion

import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.core.traversal.ModelTraversalRegistry
import amf.plugins.features.validation.CoreValidations.RecursiveShapeSpecification

import scala.collection.mutable.ListBuffer

class RecursionErrorRegister(errorHandler: ErrorHandler) {
  private val errorRegister = ListBuffer[String]()

  private def buildRecursion(base: Option[String], s: Shape): RecursiveShape = {
    val fixPointId = base.getOrElse(s.id)
    val r          = RecursiveShape(s).withFixPoint(fixPointId)
    r
  }

  def recursionAndError(root: Shape,
                        base: Option[String],
                        s: Shape,
                        traversal: ModelTraversalRegistry): RecursiveShape = {
    val recursion = buildRecursion(base, s)
    recursionError(root, recursion, traversal: ModelTraversalRegistry, Some(root.id))
  }

  def recursionError(original: Shape,
                     r: RecursiveShape,
                     traversal: ModelTraversalRegistry,
                     checkId: Option[String] = None): RecursiveShape = {

    val canRegister = !errorRegister.contains(r.id)
    if (!r.supportsRecursion.option().getOrElse(false) && !traversal.avoidError(r, checkId) && canRegister) {
      errorHandler.violation(
        RecursiveShapeSpecification,
        original.id,
        None,
        "Error recursive shape",
        original.position(),
        original.location()
      )
      errorRegister += r.id
    } else if (traversal.avoidError(r, checkId)) r.withSupportsRecursion(true)
    r
  }
}
