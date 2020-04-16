package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.model.domain.{RecursiveShape, Shape}
import amf.plugins.domain.shapes.resolution.stages.shape_normalization.RecursionPropagation._

object RecursionPropagation {
  val REJECT_ALL: RecursiveShape => Boolean = (_: RecursiveShape) => false
  val ACCEPT_ALL: RecursiveShape => Boolean = (_: RecursiveShape) => true
}

trait ClosureHelper {
  protected def addClosure(closure: Shape, target: Shape): Unit = {
    target.closureShapes.retain(_.name.value() != closure.name.value())
    target.closureShapes += closure
  }

  protected def addClosures(closures: Seq[Shape], target: Shape): Unit = {
    var closuresToAdd: List[Shape] = closures.toList
    while (closuresToAdd.nonEmpty) {
      val closure = closuresToAdd.head
      closuresToAdd = closuresToAdd.tail
      addClosure(closure, target)
      val nestedClosures =
        closure.closureShapes.filter(c => !target.closureShapes.contains(c) && !closuresToAdd.contains(c))
      closuresToAdd ++= nestedClosures
    }
  }

  protected def addFixpointToClosures(source: RecursiveShape, target: Shape): Unit = {
    source.fixpointTarget.foreach(fixpoint => addClosure(fixpoint, target))
  }

  def propagateClosures(source: Shape, target: Shape): Unit = addClosures(source.closureShapes.toSeq, target)

  def handleClosures(source: Shape,
                     target: Shape,
                     propagateRecursionClosures: RecursiveShape => Boolean = ACCEPT_ALL): Unit = {
    source match {
      case rec: RecursiveShape if propagateRecursionClosures(rec) =>
        addFixpointToClosures(rec, target)
        propagateClosures(rec, target)
      case rec: RecursiveShape =>
        addFixpointToClosures(rec, target)
      case other =>
        propagateClosures(other, target)
    }
  }

  def updateClosure(source: Shape, predicate: Shape => Boolean, newClosure: Shape): Unit = {
    source.closureShapes.find(predicate) match {
      case Some(x) =>
        source.closureShapes.remove(x)
        source.closureShapes.add(newClosure)
      case _ => // Nothing to do
    }
  }
}
