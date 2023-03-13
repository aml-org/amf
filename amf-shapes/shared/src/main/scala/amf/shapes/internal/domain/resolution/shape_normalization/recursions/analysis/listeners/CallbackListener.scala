package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners

import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.Analysis
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.ReadOnlyStack

case class CallbackListener(callback: ReadOnlyStack => Unit) extends AnalysisListener {
  override def onRecursion(stack: ReadOnlyStack)(implicit analysis: Analysis): Unit = callback(stack)
}
