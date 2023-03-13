package amf.shapes.internal.domain.resolution.shape_normalization
import amf.core.client.scala.model.domain.Shape
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners.{RecursiveErrorReporter, RecursiveShapePlacer}
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.Analysis

case class ShapeNormalizationRecursionAnalyzer(context: NormalizationContext) {
  private lazy val analysis = Analysis(RecursiveErrorReporter(context.errorHandler), RecursiveShapePlacer)

  def analyze(shape: Shape): Shape = {
    analysis.analyze(shape)
    shape
  }
}
