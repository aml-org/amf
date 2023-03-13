package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis

import amf.shapes.client.scala.model.domain.UnionShape
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners.AnalysisListener
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.BottomFrame

class UnionEnablesCyclesAnalysis(listeners: Seq[AnalysisListener]) extends Analysis(listeners)

object UnionEnablesCyclesAnalysis {
  def apply(union: UnionShape, listeners: AnalysisListener*): UnionEnablesCyclesAnalysis = {
    val analysis    = new UnionEnablesCyclesAnalysis(listeners)
    val bottomFrame = BottomFrame(union)
    analysis.stack.push(bottomFrame)
    analysis
  }
}
