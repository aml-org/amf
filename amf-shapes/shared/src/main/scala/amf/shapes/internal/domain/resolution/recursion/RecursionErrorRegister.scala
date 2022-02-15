package amf.shapes.internal.domain.resolution.recursion

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.client.scala.traversal.{ModelTraversalRegistry, ShapeTraversalRegistry}
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification

import scala.collection.mutable.ListBuffer

class RecursionErrorRegister(errorHandler: AMFErrorHandler) {
  private val errorRegister = ListBuffer[String]()

  def buildRecursion(base: Option[String], s: Shape): RecursiveShape = {
    val fixPointId = base.getOrElse(s.id)
    val r          = RecursiveShape(s).withFixPoint(fixPointId)
    r
  }

  def allowedInTraversal(traversal: ShapeTraversalRegistry,
                         r: RecursiveShape,
                         checkId: Option[String] = None): Boolean = {
    val recursiveShapeIsAllowListed = traversal.isAllowListed(r.id)
    val fixpointIsAllowListed       = r.fixpoint.option().exists(traversal.isAllowListed)
    /***
      * TODO (Refactor needed)
      * When calling ShapeExpander `checkId` some times gets set to the root shape ID from where the traversal started.
      * Why do we need to opiotnally check if this root id is allow listed? Doesn't it suffice with checking the
      * recursive shape ID or its fixpoint?
      */
    val checkIdIsAllowListed        = checkId.exists(traversal.isAllowListed)
    recursiveShapeIsAllowListed || fixpointIsAllowListed || checkIdIsAllowListed
  }

  def checkRecursionError(root: Shape,
                          r: RecursiveShape,
                          traversal: ShapeTraversalRegistry,
                          checkId: Option[String] = None,
                          criteria: ThrowRecursionValidationCriteria = DefaultCriteria()): RecursiveShape = {

    val hasNotRegisteredItYet = !errorRegister.contains(r.id)
    if (criteria.shouldThrowFor(r) && !allowedInTraversal(traversal, r, checkId) && hasNotRegisteredItYet) {
      errorHandler.violation(
        RecursiveShapeSpecification,
        root.id,
        None,
        "Error recursive shape",
        root.position(),
        root.location()
      )
      errorRegister += r.id
    } else if (allowedInTraversal(traversal, r, checkId)) r.withSupportsRecursion(true)
    r
  }
}

trait ThrowRecursionValidationCriteria {
  def shouldThrowFor(r: RecursiveShape): Boolean
}

case class DefaultCriteria() extends ThrowRecursionValidationCriteria {
  override def shouldThrowFor(r: RecursiveShape): Boolean = !r.supportsRecursion.option().getOrElse(false)
}

case class LinkableCriteria(root: Shape, linkable: Shape) extends ThrowRecursionValidationCriteria {
  override def shouldThrowFor(r: RecursiveShape): Boolean = linkable.linkTarget match {
    case Some(element) => element.id.equals(root.id)
    case None          => false
  }
}
