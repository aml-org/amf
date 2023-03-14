package amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames

import amf.core.client.scala.model.domain.Shape

object BottomFrame {
  def apply(frame: Frame): BottomFrame = BottomFrame(frame.shape)
}

case class BottomFrame(shape: Shape) extends Frame {
  override def toString: String = s"Frame($prettyPrintShape)"
}
