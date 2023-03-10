package amf.shapes.internal.domain.resolution
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject}
import amf.core.client.scala.traversal.iterator.{AmfIterator, InstanceCollector, VisitedCollector}

case class AmfUpdaterIterator private (
    var buffer: List[AmfElement],
    updater: AmfElement => AmfElement
) extends AmfIterator {
  private val visited: VisitedCollector = InstanceCollector()

  override def hasNext: Boolean = buffer.nonEmpty

  override def next: AmfElement = {
    val current = buffer.head
    buffer = buffer.tail
    if (!visited.visited(current)) {
      val next = nextElements(current)
        .map(updater)
        .filter(!visited.visited(_))
      buffer = buffer ++ next
      visited += current
      current
    } else {
      next
    }

  }

  private def nextElements(e: AmfElement): List[AmfElement] = {
    e match {
      case obj: AmfObject => obj.fields.fields().map(_.element).toList
      case arr: AmfArray  => arr.values.toList
      case _              => Nil
    }
  }
}

object AmfUpdaterIterator {
  def apply(unit: BaseUnit, updater: AmfElement => AmfElement): AmfUpdaterIterator = {
    AmfUpdaterIterator(List(unit), updater)
  }
}
