package amf.shapes.internal.domain.resolution

import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.internal.annotations.TrackedElement
import amf.shapes.client.scala.model.domain.{AnyShape, Example}

// TODO tidy up code
object ExampleTracking {

  def removeTracking(shape: Shape, id: String): Unit = shape match {
    case a: AnyShape =>
      a.examples.foreach { e =>
        e.annotations.find(classOf[TrackedElement]).foreach { te =>
          e.annotations.reject(_.isInstanceOf[TrackedElement])
          e.annotations += TrackedElement(te.parents - id)
        }
      }
    case _ => // ignore
  }

  def replaceTracking(shape: Shape, newId: AmfObject, mustExistId: String): Shape = {
    shape match {
      case a: AnyShape =>
        a.examples.foreach {
          case example if example.isLink =>
            example.annotations += tracked(newId, example, Some(mustExistId))
          case example =>
            example.annotations
              .find(classOf[TrackedElement])
              .filter(_.parents.contains(mustExistId))
              .foreach { _ =>
                example.annotations += tracked(newId, example, None)
              }
        }
      case _ => // ignore
    }
    shape
  }

  def tracking(shape: Shape, parent: AmfObject, remove: Option[String] = None): Shape = {
    shape match {
      case a: AnyShape => a.examples.foreach(e => e.annotations += tracked(parent, e, remove))
      case _           => // ignore
    }
    shape
  }

  def tracked(parent: AmfObject, e: Example, remove: Option[String]): TrackedElement =
    e.annotations
      .find(classOf[TrackedElement])
      .fold(TrackedElement.fromInstance(parent)) { t =>
        e.annotations.reject(_.isInstanceOf[TrackedElement])
        val withElement = t.addElement(parent)
        remove.map(withElement.removeId).getOrElse(withElement)
      }
}
