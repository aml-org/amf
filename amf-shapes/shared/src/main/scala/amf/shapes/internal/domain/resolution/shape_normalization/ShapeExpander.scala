package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.traversal.ShapeTraversalRegistry
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel._
import amf.shapes.internal.domain.resolution.recursion.{LinkableCriteria, RecursionErrorRegister}

private[resolution] object ShapeExpander {
  def apply(s: Shape, context: NormalizationContext, recursionRegister: RecursionErrorRegister): Shape =
    new ShapeExpander(s, recursionRegister: RecursionErrorRegister)(context).normalize()
}

sealed case class ShapeExpander(root: Shape, recursionRegister: RecursionErrorRegister)(
    implicit val context: NormalizationContext)
    extends ShapeNormalizer {

  def normalize(): Shape = normalize(root)

  protected val traversal: ShapeTraversalRegistry =
    ShapeTraversalRegistry().withAllowedCyclesInstances(Seq(classOf[UnresolvedShape]))

  protected def ensureHasId(shape: Shape): Unit = {
    if (Option(shape.id).isEmpty) {
      context.errorHandler.violation(TransformationValidation,
                                     shape.id,
                                     None,
                                     s"Resolution error: Found shape without ID: $shape",
                                     shape.position(),
                                     shape.location())
    }
  }

  private def recursiveNormalization(shape: Shape): Shape = traversal.runNested(_ => normalize(shape))

  override def normalizeAction(shape: Shape): Shape = {
    shape match {
      case l: Linkable if l.isLink =>
        /***
          * TODO: (Refactor needed)
          * Why do we create a recursive shape when we find a linkable? Shouldn't this be subject only to traversals?
          * The motivation is not explicit in the code. There is for sure some corner case where this case is needed.
          * After finding the cocrete case please extract this to a function and make explicit the conditions where
          * this is needed, otherwise delete this code.
          */
        val recursiveShape = recursionRegister.buildRecursion(Some(root.id), shape)
        recursionRegister.checkRecursionError(root,
                                              recursiveShape,
                                              traversal,
                                              Some(root.id),
                                              LinkableCriteria(root, shape))
        recursiveShape

      case _ if traversal.foundRecursion(root, shape) && !shape.isInstanceOf[RecursiveShape] =>
        val recursiveShape = recursionRegister.buildRecursion(None, shape)
        recursionRegister.checkRecursionError(root, recursiveShape, traversal, Some(root.id))
        recursiveShape

      case _ if traversal.wasVisited(shape.id) => shape

      case _ =>
        ensureHasId(shape)
        traversal + shape.id
        traversal.runNested(_ => {
          shape match {
            case union: UnionShape         => expandUnion(union)
            case scalar: ScalarShape       => expandAny(scalar)
            case array: ArrayShape         => expandArray(array)
            case matrix: MatrixShape       => expandMatrix(matrix)
            case tuple: TupleShape         => expandTuple(tuple)
            case property: PropertyShape   => expandProperty(property)
            case fileShape: FileShape      => expandAny(fileShape)
            case nil: NilShape             => nil
            case node: NodeShape           => expandNode(node)
            case recursive: RecursiveShape => recursionRegister.checkRecursionError(recursive, recursive, traversal)
            case any: AnyShape             => expandAny(any)
          }
        })
    }
  }

  protected def expandInherits(shape: Shape): Unit = {
    val oldInherits = shape.fields.getValue(ShapeModel.Inherits)
    if (Option(oldInherits).isDefined) {
      // in this case i use the father shape id and position, because the inheritance could be a recursive shape already
      val newInherits = shape.inherits.map {
        case r: RecursiveShape if r.fixpoint.option().exists(_.equals(shape.id)) =>
          recursionRegister.checkRecursionError(shape, r, traversal) // direct recursion
        case r: RecursiveShape =>
          r
        case parent =>
          val normalizedParent = recursiveNormalization(parent)
          normalizedParent
      }
      shape.setArrayWithoutId(ShapeModel.Inherits, newInherits, oldInherits.annotations)
    }
  }

  protected def expandLogicalConstraints(shape: Shape): Unit = {
    var oldLogicalConstraints = shape.fields.getValue(ShapeModel.And)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.and.map { elem =>
        val constraint = recursiveNormalization(elem)
        constraint
      }
      shape.setArrayWithoutId(ShapeModel.And, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Or)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.or.map { elem =>
        val constraint = recursiveNormalization(elem)
        constraint
      }
      shape.setArrayWithoutId(ShapeModel.Or, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Xone)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.xone.map { elem =>
        val constraint = recursiveNormalization(elem)
        constraint
      }
      shape.setArrayWithoutId(ShapeModel.Xone, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    val notConstraint = shape.fields.getValue(ShapeModel.Not)
    if (Option(notConstraint).isDefined) {
      val constraint = recursiveNormalization(shape.not)
      shape.setWithoutId(ShapeModel.Not, constraint, notConstraint.annotations)
    }
  }

  protected def expandAny(any: AnyShape): AnyShape = {
    expandInherits(any)
    expandLogicalConstraints(any)
    any
  }

  protected def expandArray(array: ArrayShape): ArrayShape = {
    expandInherits(array)
    expandLogicalConstraints(array)
    val mandatory = array.minItems.option().exists(_ > 0)
    val oldItems  = array.fields.getValue(ArrayShapeModel.Items)
    if (mandatory)
      array.inherits.collect({ case arr: ArrayShape if arr.items.isInstanceOf[RecursiveShape] => arr }).foreach { f =>
        val r = f.items.asInstanceOf[RecursiveShape]
        recursionRegister.checkRecursionError(array, r, traversal, Some(array.id))
      }
    if (Option(oldItems).isDefined) {
      val newItems = if (mandatory) {
        recursiveNormalization(array.items)
      } else { // min items not present, could be an empty array, so not need to report recursive violation
        traverseOptionalShapeFacet(array.items, array)
      }

      array.fields.setWithoutId(ArrayShapeModel.Items, newItems, oldItems.annotations)
    }
    array
  }

  protected def expandMatrix(matrix: MatrixShape): MatrixShape = {
    expandLogicalConstraints(matrix)
    val oldItems = matrix.fields.getValue(MatrixShapeModel.Items)
    if (Option(oldItems).isDefined) {
      val arrangement = recursiveNormalization(matrix.items)
      matrix.fields.setWithoutId(MatrixShapeModel.Items, arrangement, oldItems.annotations)
    }
    matrix
  }

  protected def expandTuple(tuple: TupleShape): TupleShape = {
    expandInherits(tuple)
    expandLogicalConstraints(tuple)
    val oldItems = tuple.fields.getValue(TupleShapeModel.TupleItems)
    if (Option(oldItems).isDefined) {
      val newItemShapes = tuple.items.map { item =>
        val newItem = recursiveNormalization(item)
        newItem
      }
      tuple.setArrayWithoutId(TupleShapeModel.TupleItems, newItemShapes, oldItems.annotations)
    }
    tuple
  }

  protected def expandNode(node: NodeShape): NodeShape = {
    val oldProperties = node.fields.getValue(NodeShapeModel.Properties)
    if (Option(oldProperties).isDefined) {
      val newProperties = node.properties.map { prop =>
        val newPropertyShape =
          if (isRequired(prop)) recursiveNormalization(prop).asInstanceOf[PropertyShape]
          else traverseOptionalShapeFacet(prop, node).asInstanceOf[PropertyShape]
        newPropertyShape
      }
      node.setArrayWithoutId(NodeShapeModel.Properties, newProperties, oldProperties.annotations)
    }
    Option(node.additionalPropertiesSchema).foreach(x => {
      val resultantShape = traverseOptionalShapeFacet(x, node)
      node.setWithoutId(NodeShapeModel.AdditionalPropertiesSchema, resultantShape)
    })

    expandInherits(node)
    expandLogicalConstraints(node)

    // We make explicit the implicit fields
    node.fields.entry(NodeShapeModel.Closed) match {
      case Some(entry) =>
        node.fields.setWithoutId(NodeShapeModel.Closed, entry.value.value, entry.value.annotations += ExplicitField())
      case None => node.set(NodeShapeModel.Closed, AmfScalar(false), Annotations() += ExplicitField())
    }

    node
  }

  private def isRequired(prop: PropertyShape) = prop.minCount.option().forall(_ > 0)

  protected def expandProperty(property: PropertyShape): PropertyShape = {
    // property is mandatory and must be explicit
    var required: Boolean = false
    property.fields.entry(PropertyShapeModel.MinCount) match {
      case None => // throw new Exception("MinCount field is mandatory in a shape")
      case Some(entry) =>
        entry.value.annotations += ExplicitField() // so we don't use the '?' shortcut in raml
        if (entry.value.value.asInstanceOf[AmfScalar].toNumber.intValue() != 0) {
          required = true
        }
    }

    val oldRange = property.fields.getValue(PropertyShapeModel.Range)
    if (Option(oldRange).isDefined) {
      val expandedRange = recursiveNormalization(property.range)
      property.fields.setWithoutId(PropertyShapeModel.Range, expandedRange, oldRange.annotations)
    } else {
      context.errorHandler.violation(
        TransformationValidation,
        property.id,
        s"Resolution error: Property shape with missing range: $property",
        property.annotations
      )
    }
    property
  }

  private def traverseOptionalShapeFacet(shape: Shape, from: Shape) = shape match {
    case _ if shape.inherits.nonEmpty =>
      traversal.allow(shape.inherits.map(_.id).toSet + root.id)(() => normalize(shape))
    case _: RecursiveShape => shape
    case _                 => traversal.allow(traversal.currentPath + from.id)(() => normalize(shape))
  }

  protected def expandUnion(union: UnionShape): Shape = {
    expandInherits(union)
    val oldAnyOf = union.fields.getValue(UnionShapeModel.AnyOf)
    if (Option(oldAnyOf).isDefined) {
      val newAnyOf = union.anyOf.map { u =>
        val unionMember = traversal.allow(traversal.currentPath + u.id)(() => recursiveNormalization(u))
        unionMember
      }
      union.setArrayWithoutId(UnionShapeModel.AnyOf, newAnyOf, oldAnyOf.annotations)
    } else if (Option(union.inherits).isEmpty || union.inherits.isEmpty) {
      context.errorHandler.violation(TransformationValidation,
                                     union.id,
                                     s"Resolution error: Union shape with missing anyof: $union",
                                     union.annotations)
    }

    union
  }

}
