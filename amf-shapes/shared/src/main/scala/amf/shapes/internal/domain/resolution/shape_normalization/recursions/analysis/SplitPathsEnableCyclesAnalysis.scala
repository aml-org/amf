package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis

import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners.AnalysisListener
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.BottomFrame

class SplitPathsEnableCyclesAnalysis(listeners: Seq[AnalysisListener]) extends Analysis(listeners)

object SplitPathsEnableCyclesAnalysis {
  def apply(anyShape: AnyShape, listeners: AnalysisListener*): SplitPathsEnableCyclesAnalysis = {
    val analysis    = new SplitPathsEnableCyclesAnalysis(listeners)
    val bottomFrame = BottomFrame(anyShape)
    analysis.stack.push(bottomFrame)
    analysis
  }
}
