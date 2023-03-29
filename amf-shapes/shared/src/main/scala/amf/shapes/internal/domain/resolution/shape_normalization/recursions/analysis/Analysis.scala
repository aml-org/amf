package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, RecursiveShape, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners.AnalysisListener
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.MutableStack
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.BottomFrame

class Analysis(var listeners: Seq[AnalysisListener]) {
  val stack: MutableStack                 = MutableStack()
  private var alreadyAnalyzed: Set[Shape] = Set.empty

  def analyze(shape: Shape): Unit = {
    stack.push(BottomFrame(shape))
    analyzeReferencesIn(shape)
    stack.pop()
  }

  def analyze(shape: Shape, field: Field): Unit = {
    if (shape != null) { // TODO: There's a bug in reference resolution where self references in OAS ends up with `null` values in the model
      val alreadyInStack = stack.contains(shape.id)

      stack.push(shape, field) // we push to the stack regardless if it is already in it because we need the `field`

      if (alreadyInStack && !isAllowedMultipleTimesInStack(shape)) {
        notifyListeners()
      } else {
        analyzeReferencesIn(shape)
      }

      stack.pop()
    }
  }

  private def notifyListeners(): Unit = {
    listeners.foreach { listener =>
      listener.onRecursion(stack.readOnly())(this)
    }
  }

  private def isAllowedMultipleTimesInStack(shape: Shape): Boolean = {
    shape match {
      case _: PropertyShape => true
      case _                => false
    }
  }

  private def analyzeReferencesIn(shape: Shape): Unit = {
    ifNotAnalyzed(shape) {
      case union: UnionShape       => analyzeReferencesInUnion(union)
      case scalar: ScalarShape     => analyzeReferencesInShape(scalar)
      case array: ArrayShape       => analyzeReferencesInArray(array)
      case matrix: MatrixShape     => analyzeReferencesInMatrix(matrix)
      case tuple: TupleShape       => analyzeReferencesInTuple(tuple)
      case property: PropertyShape => analyzeReferencesInProperty(property)
      case fileShape: FileShape    => analyzeReferencesInShape(fileShape)
      case nil: NilShape           => analyzeReferencesInShape(nil)
      case node: NodeShape         => analyzeReferencesInNode(node)
      case any: AnyShape           => analyzeReferencesInShape(any)
      case _: RecursiveShape       => // ignore
    }
  }

  private def ifNotAnalyzed(shape: Shape)(fn: Shape => Unit): Unit = {
    if (!alreadyAnalyzed.contains(shape)) {
      fn(shape)
      alreadyAnalyzed = alreadyAnalyzed + shape
    }
  }

  private def analyzeReferencesInShape(shape: Shape): Unit = analyzeReferencesInLogicalConstraints(shape)

  private def analyzeReferencesInNode(node: NodeShape): Unit = {
    analyzeReferencesInLogicalConstraints(node)
    analyzeReferencesInProperties(node)
    analyzeReferencesInAdditionalPropertiesSchema(node)
  }

  private def analyzeReferencesInProperty(property: PropertyShape): Unit =
    analyze(property.range, property.meta.Range)

  private def analyzeReferencesInArray(array: ArrayShape): Unit = {
    analyzeReferencesInLogicalConstraints(array)
    array.items match {
      case items: Shape => analyze(items, array.meta.Items)
      case _            => // nothing
    }
  }

  private def analyzeReferencesInMatrix(matrix: MatrixShape): Unit = {
    analyzeReferencesInLogicalConstraints(matrix)
    matrix.items match {
      case items: Shape => analyze(items, matrix.meta.Items)
      case _            => // nothing
    }
  }

  private def analyzeReferencesInTuple(tuple: TupleShape): Unit = {
    analyzeReferencesInLogicalConstraints(tuple)
    tuple.items.foreach(shape => analyze(shape, tuple.meta.TupleItems))
  }

  private def analyzeReferencesInUnion(union: UnionShape): Unit = {
    union.anyOf.foreach(s => analyze(s, union.meta.AnyOf))
  }

  private def analyzeReferencesInLogicalConstraints(shape: Shape): Unit = {
    analyzeReferencesInLogicalConstraint(shape, ShapeModel.And)
    analyzeReferencesInLogicalConstraint(shape, ShapeModel.Or)
    analyzeReferencesInLogicalConstraint(shape, ShapeModel.Xone)
    analyzeReferencesInLogicalConstraint(shape, ShapeModel.Not)
  }

  private def analyzeReferencesInLogicalConstraint(shape: Shape, constraintField: Field): Unit = {
    shape.fields.getValueAsOption(constraintField) match {
      case Some(constraint) =>
        constraint.value match {
          case array: AmfArray =>
            array.values.foreach { e => analyze(e.asInstanceOf[Shape], constraintField) }
          case s: Shape => analyze(s, constraintField)
          case _        =>
        }
      case _ =>
    }
  }

  private def analyzeReferencesInProperties(node: NodeShape): Unit =
    node.properties.foreach(s => analyze(s, node.meta.Properties))

  private def analyzeReferencesInAdditionalPropertiesSchema(node: NodeShape): Unit = {
    Option(node.additionalPropertiesSchema).foreach(s => analyze(s, node.meta.AdditionalPropertiesSchema))
  }

  def append(listener: AnalysisListener): Unit = {
    listeners = listeners :+ listener
  }
}
object Analysis {
  def apply(listeners: AnalysisListener*) = new Analysis(listeners)
}
