package amf.plugins.domain.shapes.models

import amf.core.annotations.TrackedElement
import amf.core.model.domain.Shape

object ExampleTracking {

  def tracking(shape: Shape, parent: String): Shape = {
    shape match {
      case a: AnyShape => a.examples.foreach(_.annotations += TrackedElement(parent))
      case _           => // ignore
    }
    shape
  }
}
