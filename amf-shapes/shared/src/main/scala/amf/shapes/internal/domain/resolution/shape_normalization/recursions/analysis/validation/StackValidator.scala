package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.validation

import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.Analysis
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.ReadOnlyStack
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.MiddleFrame

object StackValidator extends ShapeHelper with FieldHelper {
  def containsValidCycle(stack: ReadOnlyStack)(implicit analysis: Analysis): Boolean = {
    stack.toSeq.exists {
      case MiddleFrame(shape, field) => fieldEnablesCycles(field) || shapeEnablesCycles(shape)
      case _                         => false
    }
  }
}
