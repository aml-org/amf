package amf.shapes.internal.domain.resolution
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, DataNode}
import amf.core.client.scala.traversal.iterator.{AmfIterator, InstanceCollector, VisitedCollector}
import amf.shapes.client.scala.model.domain.Example

import scala.collection.immutable.Queue

case class AmfUpdaterIterator private (var buffer: Queue[AmfElement], updater: AmfElement => AmfElement)
    extends AmfIterator {
  private val visited: VisitedCollector = InstanceCollector()

  /** This is a performance improvement. We are interested only in traversing AmfElements in which we could find a Shape
    * that might need to be updated in their sub-tree. That said, we know (from the model) that some elements are
    * dead-ends. This method hints the traversal to skip these elements to improve performance.
    */
  private def shouldSkip(o: AmfElement): Boolean = {
    o match {
      case _: Example  => true
      case _: DataNode => true
      case _           => false
    }
  }

  override def hasNext: Boolean = {
    if (buffer.nonEmpty) {
      val current = buffer.head
      if (visited.visited(current)) {
        buffer = buffer.tail
        hasNext
      } else true

    } else false
  }

  override def next: AmfElement = {
    val current = buffer.head
    buffer = buffer.tail
    if (!visited.visited(current)) {
      val next = nextElements(current)
        .map(updater)
        .filter(e => !visited.visited(e) && !shouldSkip(e))
      buffer = buffer ++ next
      visited += current
      current
    } else {
      next
    }

  }

  private def nextElements(e: AmfElement): Iterable[AmfElement] = {
    e match {
      case obj: AmfObject => obj.fields.fieldsValues().map(_.value)
      case arr: AmfArray  => arr.values
      case _              => Nil
    }
  }
}

object AmfUpdaterIterator {
  def apply(unit: BaseUnit, updater: AmfElement => AmfElement): AmfUpdaterIterator = {
    AmfUpdaterIterator(Queue(unit), updater)
  }
}
