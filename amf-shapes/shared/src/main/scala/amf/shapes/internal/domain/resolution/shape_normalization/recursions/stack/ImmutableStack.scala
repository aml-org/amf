package amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack

import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.Frame

case class ImmutableStack() extends Stack() {
  override protected type PopReturnType  = (Frame, ImmutableStack)
  override protected type PushReturnType = ImmutableStack
  override protected def doPop(): (Frame, ImmutableStack)     = (peek(), ImmutableStack(stack.tail))
  override protected def doPush(frame: Frame): ImmutableStack = ImmutableStack(frame +: stack)
}

object ImmutableStack {
  def apply(stack: Seq[Frame]): ImmutableStack = {
    val roStack = ImmutableStack()
    roStack.stack = stack
    roStack
  }
}
