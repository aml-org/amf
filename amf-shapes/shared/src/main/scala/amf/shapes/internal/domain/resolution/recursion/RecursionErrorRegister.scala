package amf.shapes.internal.domain.resolution.recursion

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.client.scala.traversal.ModelTraversalRegistry
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification

import scala.collection.mutable.ListBuffer

class RecursionErrorRegister(errorHandler: AMFErrorHandler) {
  private val errorRegister = ListBuffer[String]()

  private def buildRecursion(base: Option[String], s: Shape): RecursiveShape = {
    val fixPointId = base.getOrElse(s.id)
    val r          = RecursiveShape(s).withFixPoint(fixPointId)
    r
  }

  def recursionAndError(root: Shape,
                        base: Option[String],
                        s: Shape,
                        traversal: ModelTraversalRegistry,
                        criteria: RegisterCriteria = DefaultRegisterCriteria()): RecursiveShape = {
    val recursion = buildRecursion(base, s)
    recursionError(root, recursion, traversal: ModelTraversalRegistry, Some(root.id), criteria)
  }

  def recursionError(original: Shape,
                     r: RecursiveShape,
                     traversal: ModelTraversalRegistry,
                     checkId: Option[String] = None,
                     criteria: RegisterCriteria = DefaultRegisterCriteria()): RecursiveShape = {

    val hasNotRegisteredItYet = !errorRegister.contains(r.id)
    if (criteria.decide(r) && !traversal.avoidError(r, checkId) && hasNotRegisteredItYet) {
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

trait RegisterCriteria {
  def decide(r: RecursiveShape): Boolean
}

case class DefaultRegisterCriteria() extends RegisterCriteria {
  override def decide(r: RecursiveShape): Boolean = !r.supportsRecursion.option().getOrElse(false)
}

case class LinkableRegisterCriteria(root: Shape, linkable: Shape) extends RegisterCriteria {
  override def decide(r: RecursiveShape): Boolean = linkable.linkTarget match {
    case Some(element) => element.id.equals(root.id)
    case None          => false
  }
}
