package amf.shapes.internal.domain.resolution.shape_normalization2
import amf.core.client.scala.model.domain.{PerpetualAnnotation, Shape}

object UnnecessaryAnnotationsRemover {
   def apply(shape: Shape): Unit = {
    shape.annotations.reject(a => !a.isInstanceOf[PerpetualAnnotation])
  }
}
