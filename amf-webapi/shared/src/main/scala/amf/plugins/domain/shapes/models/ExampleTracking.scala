package amf.plugins.domain.shapes.models

import amf.core.annotations.TrackedElement
import amf.core.model.domain.Shape

object ExampleTracking {

  def tracking(shape: Shape, parent: String, remove: Option[String] = None): Shape = {
    shape match {
      case a: AnyShape =>
        a.examples match {
          case examples: Examples if examples.isLink => examples.annotations += tracked(parent, examples, remove)
          case _: Examples                           => a.exampleValues.foreach(e => e.annotations += tracked(parent, e, remove))
          case _                                     => // ignore
        }
      case _ => // ignore
    }
    shape
  }

  private def tracked(parent: String, e: Example, remove: Option[String]): TrackedElement =
    e.annotations
      .find(classOf[TrackedElement])
      .fold(TrackedElement(parent)) { t =>
        e.annotations.reject(_.isInstanceOf[TrackedElement])
        TrackedElement(t.parents + parent -- remove)
      }

  private def tracked(parent: String, e: Examples, remove: Option[String]): TrackedElement =
    e.annotations
      .find(classOf[TrackedElement])
      .fold(TrackedElement(parent)) { t =>
        e.annotations.reject(_.isInstanceOf[TrackedElement])
        TrackedElement(t.parents + parent -- remove)
      }
}
