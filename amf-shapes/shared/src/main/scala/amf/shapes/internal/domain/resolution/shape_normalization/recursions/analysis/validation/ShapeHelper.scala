package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.validation

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.Field
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NilShape, ScalarShape, UnionShape}
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners.CallbackListener
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.{
  Analysis,
  SplitPathsEnableCyclesAnalysis
}
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.ReadOnlyStack

trait ShapeHelper {
  def shapeEnablesCycles(shape: Shape)(implicit analysis: Analysis): Boolean = {
    shape match {
      case p: PropertyShape                         => p.minCount.value() == 0
      case a: ArrayShape                            => a.minItems.value() == 0
      case _: NilShape                              => true
      case _: UnionShape if alreadyAnalyzingUnion() => false
      case u: UnionShape                            => analyzeSplitPaths(u, u.meta.AnyOf)
      case a: AnyShape if a.xone.nonEmpty           => analyzeSplitPaths(a, a.meta.Xone)
      case a: AnyShape if a.or.nonEmpty             => analyzeSplitPaths(a, a.meta.Or)
      case _                                        => false
    }
  }

  private def alreadyAnalyzingUnion()(implicit analysis: Analysis) =
    analysis.isInstanceOf[SplitPathsEnableCyclesAnalysis]

  /** Unions, oneOfs and anyOfs allow cycles if AT LEAST ONE of the "different paths" (stacks under each union member)
    * allows cycles. To determine that we need to start a new sub-analysis if we aren't already in one
    */
  private def analyzeSplitPaths(anyShape: AnyShape, field: Field): Boolean = {
    object PerformanceOrder extends Ordering[Shape] {
      // Lower nr. is higher priority
      private def priority(s: Shape): Int = {
        s match {
          case _: ScalarShape | _: NilShape => 0
          case _                            => 1
        }
      }
      override def compare(x: Shape, y: Shape): Int = priority(x).compare(priority(y))
    }

    val members: Seq[Shape] = anyShape.fields.field(field)

    members.sorted(PerformanceOrder).exists {
      case _: ScalarShape => true
      case _: NilShape    => true
      case member         => memberEnablesCycles(anyShape, member, field)
    }
  }

  private def memberEnablesCycles(anyShape: AnyShape, member: Shape, field: Field): Boolean = {
    var foundCycle   = false
    var cycleIsValid = false
    val analysis     = SplitPathsEnableCyclesAnalysis(anyShape)

    // Since we cannot capture the returned value of a callback we assign variables (`foundCycle` & `cycleIsValid`)
    val callback = CallbackListener((s: ReadOnlyStack) => {
      foundCycle = true
      val (_, tail) = s.pop() // Why pop?
      cycleIsValid = StackValidator.containsValidCycle(tail)(analysis)
    })

    analysis.append(callback)

    analysis.analyze(member, field)
    !foundCycle || (foundCycle && cycleIsValid)
  }

}
