package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.validation

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.shapes.client.scala.model.domain.{ArrayShape, NilShape, UnionShape}
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners.CallbackListener
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.{
  Analysis,
  UnionEnablesCyclesAnalysis
}
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.ReadOnlyStack

trait ShapeHelper {
  def shapeEnablesCycles(shape: Shape)(implicit analysis: Analysis): Boolean = {
    shape match {
      case p: PropertyShape                         => p.minCount.value() == 0
      case a: ArrayShape                            => a.minItems.value() == 0
      case _: NilShape                              => true
      case _: UnionShape if alreadyAnalyzingUnion() => false
      case u: UnionShape                            => unionEnablesCycles(u)
      case _                                        => false
    }
  }

  private def alreadyAnalyzingUnion()(implicit analysis: Analysis) = analysis.isInstanceOf[UnionEnablesCyclesAnalysis]

  /** Unions allow cycles if AT LEAST ONE of the "different paths" (stacks under each union member) allows cycles. To
    * determine that we need to start a new sub-analysis if we aren't already in one
    */
  private def unionEnablesCycles(union: UnionShape): Boolean = {
    union.anyOf.exists { member =>
      memberEnablesCycles(union, member)
    }
  }

  private def memberEnablesCycles(union: UnionShape, member: Shape): Boolean = {
    var foundCycle   = false
    var cycleIsValid = false
    val analysis     = UnionEnablesCyclesAnalysis(union)

    // Since we cannot capture the returned value of a callback we assign variables (`foundCycle` & `cycleIsValid`)
    val callback = CallbackListener((s: ReadOnlyStack) => {
      foundCycle = true
      val (_, tail) = s.pop() // Why pop?
      cycleIsValid = StackValidator.containsValidCycle(tail)(analysis)
    })

    analysis.append(callback)

    analysis.analyze(member, union.meta.AnyOf)
    !foundCycle || (foundCycle && cycleIsValid)
  }

}
