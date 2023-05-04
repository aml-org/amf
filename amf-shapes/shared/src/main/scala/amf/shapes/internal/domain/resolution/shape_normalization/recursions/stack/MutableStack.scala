package amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.{Frame, MiddleFrame}

case class MutableStack() extends Stack() {

  override protected type PushReturnType = Unit
  override protected type PopReturnType  = Frame
  def push(shape: Shape, field: Field): Unit = push(MiddleFrame(shape, field))

  override protected def doPush(frame: Frame): Unit = { stack = frame +: stack }

  override def doPop(): Frame = {
    val head = stack.head
    stack = stack.tail
    head
  }

  def readOnly(): ImmutableStack = ImmutableStack(this.stack)
}
