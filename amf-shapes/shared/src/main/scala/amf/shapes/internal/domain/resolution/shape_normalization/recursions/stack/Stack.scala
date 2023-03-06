package amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack

import amf.core.client.scala.model.domain.Shape
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.{BottomFrame, Frame}

// This class has optimization potential
// maybe add frame cache?
// should index for lookups?
abstract class Stack(protected var stack: Seq[Frame] = Seq.empty) {
  def contains(shape: Shape): Boolean = stack.exists(frame => frame.shape == shape)

  def contains(id: String): Boolean = stack.exists(frame => frame.shape.id == id)

  def peek(idx: Int = 0): Frame = stack(idx)

  // Push
  protected type PushReturnType

  def push(frame: Frame): PushReturnType = {
    frame match {
      case _: BottomFrame if stack.nonEmpty => stateException("Tried to push a BottomFrame on a non-empty stack")
      case _                                => doPush(frame)
    }
  }

  protected def doPush(frame: Frame): PushReturnType

  // Pop
  protected type PopReturnType

  def pop(): PopReturnType = {
    if (stack.isEmpty) stateException("Tried to pop an empty stack")
    else doPop()
  }

  private def stateException(msg: String) = throw new IllegalStateException(msg)

  protected def doPop(): PopReturnType

  def toSeq: Seq[Frame] = this.stack
}
