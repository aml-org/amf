package amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.listeners

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, RecursiveShape, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.ArrayLike
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.internal.domain.metamodel.NodeShapeModel.Properties
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.analysis.Analysis
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.ReadOnlyStack
import amf.shapes.internal.domain.resolution.shape_normalization.recursions.stack.frames.{Frame, MiddleFrame}

object RecursiveShapePlacer extends AnalysisListener {
  override def onRecursion(stack: ReadOnlyStack)(implicit analysis: Analysis): Unit = {
    val currentFrame  = stack.peek().asInstanceOf[MiddleFrame] // where the loop closes
    val previousFrame = stack.peek(1)
    placeRecursiveShape(stack, currentFrame, previousFrame)
  }
  private def placeRecursiveShape(stack: ReadOnlyStack, currentFrame: MiddleFrame, previousFrame: Frame): Unit = {
    val parent         = maybeCopyParent(stack, previousFrame)
    val recursiveShape = RecursiveShape(currentFrame.shape).withSupportsRecursion(true)

    currentFrame.field match {
      case field if isArrayField(field) => setArrayFieldValue(parent, recursiveShape, field)
      case field                        => setFieldValue(parent, recursiveShape, field)
    }
  }

  private def setFieldValue(parent: Shape, recursiveShape: RecursiveShape, field: Field): Unit = {
    setFieldKeepingAnnotations(parent, field, recursiveShape)
  }

  private def setArrayFieldValue(parent: Shape, recursiveShape: RecursiveShape, field: Field): Unit = {
    val array = parent.fields.get(field).asInstanceOf[AmfArray]
    val values = array.values.map {
      case s: Shape if recursiveShape.fixpoint.is(s.id) => recursiveShape
      case other                                        => other
    }
    setArrayFieldKeepingAnnotations(parent, field, values)
  }

  private def isArrayField(field: Field) = field.`type`.isInstanceOf[ArrayLike]

  /** If we find a cycle in the range of a property shape, we copy that property and set the corresponding recursive
    * shape as its range. Why do we copy? Because we might have 2+ versions of the same property through inheritance and
    * only one should be recursive. Example (RAML):
    * {{{
    *  types:
    *    A:
    *      properties:
    *        b: B
    *    B:
    *      type: A
    *      properties:
    *        c: string
    * }}}
    * In this example `B` will inherit the `b` property from `A`. In normal inheritance there will be only 1 `b` node in
    * the graph to avoid unnecessarily duplicating properties and improve memory performance. However, in this case the
    * `b` property is only recursive in `B` not in `A`, and that's why we should copy.
    */
  private def maybeCopyParent(stack: ReadOnlyStack, previousFrame: Frame) = {
    previousFrame.shape match {
      case property: PropertyShape =>
        val propertyParent = stack.peek(2).shape.asInstanceOf[NodeShape]
        copyAndSetPropertyShapeInParent(property, propertyParent)
      case other => other
    }
  }

  private def copyAndSetPropertyShapeInParent(property: PropertyShape, parent: NodeShape): PropertyShape = {
    // Cannot clone because property is recursive
    val copy = property.copyShape().withId(property.id + "/recursiveProp")
    val newProperties = parent.properties.map {
      case p if p == property => copy
      case p                  => p
    }
    setArrayFieldKeepingAnnotations(parent, Properties, newProperties)
    copy
  }

  private def setArrayFieldKeepingAnnotations(obj: AmfObject, field: Field, value: Seq[AmfElement]): Unit = {
    obj.fields.entry(field) match {
      case Some(entry) =>
        obj.setArrayWithoutId(field, value, entry.value.annotations)
      case None =>
        obj.setArrayWithoutId(field, value, Annotations())
    }
  }

  private def setFieldKeepingAnnotations(obj: AmfObject, field: Field, value: AmfElement): Unit = {
    obj.fields.entry(field) match {
      case Some(entry) =>
        obj.setWithoutId(field, value, entry.value.annotations)
      case None =>
        obj.setWithoutId(field, value, Annotations())
    }
  }
}
