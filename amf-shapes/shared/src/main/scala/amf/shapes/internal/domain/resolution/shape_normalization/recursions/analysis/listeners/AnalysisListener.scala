package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners

import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.Analysis
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.ImmutableStack

/** Analysis listeners should not mutate the stack, only the analysis itself should. That is why these receive a
  * ReadOnlyStack
  */
trait AnalysisListener {
  def onRecursion(stack: ImmutableStack)(implicit analysis: Analysis): Unit
}
