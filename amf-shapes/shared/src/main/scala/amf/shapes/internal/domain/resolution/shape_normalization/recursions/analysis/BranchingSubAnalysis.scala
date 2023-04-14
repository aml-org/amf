package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis

import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners.AnalysisListener
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.BottomFrame

class BranchingSubAnalysis(listeners: Seq[AnalysisListener]) extends Analysis(listeners)

object BranchingSubAnalysis {
  def apply(anyShape: AnyShape, listeners: AnalysisListener*): BranchingSubAnalysis = {
    val analysis    = new BranchingSubAnalysis(listeners)
    val bottomFrame = BottomFrame(anyShape)
    analysis.stack.push(bottomFrame)
    analysis
  }
}
