package amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field

case class MiddleFrame(shape: Shape, field: Field) extends Frame {
  override def toString: String = s"Frame($prettyPrintShape, ${field.toString})"
}
