package amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack

import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.Analysis
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.Frame

case class ReadOnlyStack() extends Stack() {
  override protected type PopReturnType  = (Frame, ReadOnlyStack)
  override protected type PushReturnType = ReadOnlyStack
  override protected def doPop(): (Frame, ReadOnlyStack)     = (peek(), ReadOnlyStack(stack.tail))
  override protected def doPush(frame: Frame): ReadOnlyStack = ReadOnlyStack(frame +: stack)
}

object ReadOnlyStack {
  def apply(stack: Seq[Frame]): ReadOnlyStack = {
    val roStack = ReadOnlyStack()
    roStack.stack = stack
    roStack
  }
}
