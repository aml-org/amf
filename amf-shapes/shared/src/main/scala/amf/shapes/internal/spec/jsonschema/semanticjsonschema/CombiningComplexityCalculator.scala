package amf.shapes.internal.spec.jsonschema.semanticjsonschema

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.traversal.iterator.{AmfElementStrategy, IdCollector}
import amf.shapes.client.scala.model.domain.AnyShape

import scala.collection.mutable

class CombiningComplexityCalculator {

  private val visited: mutable.Set[String] = mutable.Set()

  def calculateComplexity(shape: AnyShape): Int = {
    val iterator = AmfElementStrategy.iterator(List(shape), IdCollector())
    val allOfs   = iterator.collect { case and: AnyShape if and.isAnd => and }
    allOfs.map(evaluateCombiningShape).sum
  }

  private def evaluateCombiningShape(shape: AnyShape): Int = {
    if (visited.add(shape.id)) {
      val combiningElements = shape.and
      // If the allOf has an extended schema (a side schema) it will add 1 to the summation and 1 to the multiplier
      val extendedSchema      = if (hasExtendedSchema(shape)) 1 else 0
      val combiningSum        = combiningElements.map(evaluateElement).sum + extendedSchema
      val combiningMultiplier = combiningElements.size + extendedSchema
      combiningSum * combiningMultiplier
    } else 0
    // Will be 0 when the shape were already evaluated inside another anyOf
  }

  private def hasExtendedSchema(shape: AnyShape) = !shape.isInstanceOf[AnyShape]

  private def evaluateElement(shape: Shape): Int = shape match {
    case and: AnyShape if and.isAnd => // For a nested allOf, it will be calculated as a combining also
      evaluateCombiningShape(and)
    case or: AnyShape if or.isOr => // For a nested oneOf, it will be calculated as the sum of the value of each element
      or.or.map(evaluateElement).sum
    case conditional: AnyShape
        if conditional.isConditional => // For a nested conditional, it will be calculated as the sum of the value of the then and the else
      Seq(Option(conditional.thenShape), Option(conditional.elseShape)).flatten.map(evaluateElement).sum
    case _ => 1 // For the rest of the cases, object, scalars, arrays, etc., it will count as 1
  }

}
