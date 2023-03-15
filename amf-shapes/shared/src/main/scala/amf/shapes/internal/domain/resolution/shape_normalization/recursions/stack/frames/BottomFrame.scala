package amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames

import amf.core.client.scala.model.domain.Shape

object BottomFrame {
  def toBottomFrame(frame: Frame): BottomFrame = {
    frame match {
      case b: BottomFrame => b
      case f: Frame       => BottomFrame(f.shape)
    }
  }
}

case class BottomFrame(shape: Shape) extends Frame {
  override def toString: String = s"Frame($prettyPrintShape)"
}
