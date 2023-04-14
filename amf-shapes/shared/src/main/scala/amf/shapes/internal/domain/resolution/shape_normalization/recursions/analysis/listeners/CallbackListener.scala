package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners

import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.Analysis
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.ImmutableStack

case class CallbackListener(callback: ImmutableStack => Unit) extends AnalysisListener {
  override def onRecursion(stack: ImmutableStack)(implicit analysis: Analysis): Unit = callback(stack)
}
