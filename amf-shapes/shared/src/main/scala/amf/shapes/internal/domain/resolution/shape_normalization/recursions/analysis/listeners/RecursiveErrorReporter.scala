package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.Analysis
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.validation.StackValidator
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.ReadOnlyStack

case class RecursiveErrorReporter(eh: AMFErrorHandler) extends AnalysisListener {
  override def onRecursion(stack: ReadOnlyStack)(implicit analysis: Analysis): Unit = {
    val shape = stack.peek().shape
    val stackOfInterest = stack.substack(shape.id)
    val cycleIsValid = StackValidator.containsValidCycle(stackOfInterest)
    if (!cycleIsValid) {
      eh.violation(
        RecursiveShapeSpecification,
        shape.id,
        None,
        s"Invalid cyclic references in shapes: ${stackTrace(stackOfInterest)}",
        shape.position(),
        shape.location()
      )
    }
  }

  private def stackTrace(stack: ReadOnlyStack): String = {
    stack.toSeq
      .map(_.shape)
      .filter {
        case _: PropertyShape => true
        case other => other.annotations.contains(classOf[DeclaredElement]) // we skip printing inline shapes for clarity
      }
      .map(_.name.value())
      .reverse
      .mkString(" -> ")
  }

}
