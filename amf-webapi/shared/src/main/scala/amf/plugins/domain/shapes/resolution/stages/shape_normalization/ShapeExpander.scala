package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.annotations.ExplicitField
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Annotations
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.resolution.stages.RecursionErrorRegister

private[stages] object ShapeExpander {
  def apply(s: Shape, context: NormalizationContext, recursionRegister: RecursionErrorRegister): Shape =
    new ShapeExpander(s, recursionRegister: RecursionErrorRegister)(context).normalize()
}

sealed case class ShapeExpander(root: Shape, recursionRegister: RecursionErrorRegister)(
    implicit val context: NormalizationContext)
    extends ShapeNormalizer {

  def normalize(): Shape = normalize(root)

  protected val traversed: IdsTraversionCheck =
    IdsTraversionCheck().withAllowedCyclesInstances(Seq(classOf[UnresolvedShape]))

  protected def ensureCorrect(shape: Shape): Unit = {
    if (Option(shape.id).isEmpty) {
      throw new Exception(s"Resolution error: Found shape without ID: $shape")
    }
  }

  private def recursiveNormalization(shape: Shape): Shape = traversed.runPushed(_ => normalize(shape))

  override def normalizeAction(shape: Shape): Shape = {
    shape match {
      case l: Linkable if l.isLink => recursionRegister.recursionAndError(root, Some(root.id), shape, traversed)

      case _ if traversed.has(shape) && !shape.isInstanceOf[RecursiveShape] =>
        recursionRegister.recursionAndError(root, None, shape, traversed)

      case _ =>
        ensureCorrect(shape)
        traversed + shape.id
        traversed.runPushed(_ => {
          shape match {
            case union: UnionShape       => expandUnion(union)
            case scalar: ScalarShape     => expandAny(scalar)
            case array: ArrayShape       => expandArray(array)
            case matrix: MatrixShape     => expandMatrix(matrix)
            case tuple: TupleShape       => expandTuple(tuple)
            case property: PropertyShape => expandProperty(property)
            case fileShape: FileShape    => expandAny(fileShape)
            case nil: NilShape           => nil
            case node: NodeShape         => expandNode(node)
            case recursive: RecursiveShape =>
              recursionRegister.recursionError(recursive, recursive, recursive.id, traversed)
            case any: AnyShape => expandAny(any)
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
          r.fixpointTarget.foreach(target => shape.closureShapes ++= Seq(target).filter(_.id != shape.id))
          recursionRegister.recursionError(shape, r, r.id, traversed) // direct recursion
        case r: RecursiveShape =>
          r.fixpointTarget.foreach(target => shape.closureShapes ++= Seq(target).filter(_.id != shape.id))
          r
        case other =>
          recursiveNormalization(other) match {
            case rec: RecursiveShape =>
              rec.fixpointTarget.foreach(target => shape.closureShapes ++= Seq(target).filter(_.id != shape.id))
              rec
            case o =>
              shape.closureShapes ++= o.closureShapes.filter(_.id != shape.id)
              o
          }
      }
      shape.setArrayWithoutId(ShapeModel.Inherits, newInherits, oldInherits.annotations)
    }
  }

  protected def expandLogicalConstraints(shape: Shape): Unit = {
    var oldLogicalConstraints = shape.fields.getValue(ShapeModel.And)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.and.map { elem =>
        val constraint = recursiveNormalization(elem)
        constraint match {
          case rec: RecursiveShape =>
            rec.fixpointTarget.foreach(target => shape.closureShapes ++= Seq(target).filter(_.id != shape.id))
          case other =>
            shape.closureShapes ++= other.closureShapes.filter(_.id != shape.id)
        }
        constraint
      }
      shape.setArrayWithoutId(ShapeModel.And, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Or)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.or.map { elem =>
        val constraint = recursiveNormalization(elem)
        constraint match {
          case rec: RecursiveShape =>
            rec.fixpointTarget.foreach(target => shape.closureShapes ++= Seq(target).filter(_.id != shape.id))
          case other =>
            shape.closureShapes ++= other.closureShapes.filter(_.id != shape.id)
        }
        constraint
      }
      shape.setArrayWithoutId(ShapeModel.Or, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Xone)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.xone.map { elem =>
        val constraint = recursiveNormalization(elem)
        constraint match {
          case rec: RecursiveShape =>
            rec.fixpointTarget.foreach(target => shape.closureShapes ++= Seq(target).filter(_.id != shape.id))
          case other =>
            shape.closureShapes ++= other.closureShapes.filter(_.id != shape.id)
        }
        constraint
      }
      shape.setArrayWithoutId(ShapeModel.Xone, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    val notConstraint = shape.fields.getValue(ShapeModel.Not)
    if (Option(notConstraint).isDefined) {
      val constraint = recursiveNormalization(shape.not)
      constraint match {
        case rec: RecursiveShape =>
          rec.fixpointTarget.foreach(target => shape.closureShapes ++= Seq(target).filter(_.id != shape.id))
        case other =>
          shape.closureShapes ++= other.closureShapes.filter(_.id != shape.id)
      }
      shape.set(ShapeModel.Not, constraint, notConstraint.annotations)
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
        recursionRegister.recursionError(array, r, array.id, traversed)
        r.fixpointTarget.foreach(target => array.closureShapes ++= Seq(target).filter(_.id != array.id))
      }
    if (Option(oldItems).isDefined) {
      val newItems = if (mandatory) {
        recursiveNormalization(array.items)
      } else { // min items not present, could be an empty array, so not need to report recursive violation
        traverseOptionalShapeFacet(array.items)
      }

      // dealing with recursion and closure
      newItems match {
        case rec: RecursiveShape =>
          rec.fixpointTarget.foreach(target => array.closureShapes ++= Seq(target).filter(_.id != array.id))
        case other =>
          array.closureShapes ++= other.closureShapes.filter(_.id != array.id)
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

      // dealing with recursion and closure
      arrangement match {
        case rec: RecursiveShape =>
          rec.fixpointTarget.foreach(target => matrix.closureShapes ++= Seq(target).filter(_.id != matrix.id))
        case other =>
          matrix.closureShapes ++= other.closureShapes.filter(_.id != matrix.id)
      }

      matrix.fields.setWithoutId(MatrixShapeModel.Items, arrangement, oldItems.annotations)
    }
    matrix
  }

  protected def expandTuple(tuple: TupleShape): TupleShape = {
    expandLogicalConstraints(tuple)
    val oldItems = tuple.fields.getValue(TupleShapeModel.TupleItems)
    if (Option(oldItems).isDefined) {
      val newItemShapes = tuple.items.map { item =>
        val newItem = recursiveNormalization(item)
        // update the closure
        newItem match {
          case rec: RecursiveShape =>
            rec.fixpointTarget.foreach(target => tuple.closureShapes ++= Seq(target).filter(_.id != tuple.id))
          case other =>
            tuple.closureShapes ++= other.closureShapes.filter(_.id != tuple.id)
        }
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
        val newPropertyShape = recursiveNormalization(prop).asInstanceOf[PropertyShape]
        // update the closure
        newPropertyShape.range match {
          case rec: RecursiveShape =>
            rec.fixpointTarget.foreach(target => node.closureShapes ++= Seq(target).filter(_.id != node.id))
          case other =>
            node.closureShapes ++= other.closureShapes.filter(_.id != node.id)

        }
        newPropertyShape
      }
      node.setArrayWithoutId(NodeShapeModel.Properties, newProperties, oldProperties.annotations)
    }

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
      val expandedRange =
        if (!required) traverseOptionalShapeFacet(property.range) else recursiveNormalization(property.range)

      property.fields.setWithoutId(PropertyShapeModel.Range, expandedRange, oldRange.annotations)
    } else {
      throw new Exception(s"Resolution error: Property shape with missing range: $property")
    }
    property
  }

  private def traverseOptionalShapeFacet(shape: Shape) = {
    shape.linkTarget match {
      case Some(t) => traversed.runWithIgnoredIds(() => normalize(shape), Set(root.id, t.id))
      case None if shape.inherits.nonEmpty =>
        traversed.runWithIgnoredIds(() => normalize(shape), shape.inherits.map(_.id).toSet + root.id)
      case _ => traversed.runWithIgnoredIds(() => normalize(shape), Set(root.id, shape.id))
    }
  }

  protected def expandUnion(union: UnionShape): Shape = {
    expandInherits(union)
    val oldAnyOf = union.fields.getValue(UnionShapeModel.AnyOf)
    if (Option(oldAnyOf).isDefined) {
      val newAnyOf = union.anyOf.map { u =>
        val unionMember = traversed.recursionAllowed(() => recursiveNormalization(u), u.id)
        unionMember match {
          case rec: RecursiveShape =>
            rec.fixpointTarget.foreach(target => union.closureShapes ++= Seq(target).filter(_.id != union.id))
          case other =>
            union.closureShapes ++= other.closureShapes.filter(_.id != union.id)
        }
        unionMember
      }
      union.setArrayWithoutId(UnionShapeModel.AnyOf, newAnyOf, oldAnyOf.annotations)
    } else if (Option(union.inherits).isEmpty || union.inherits.isEmpty) {
      throw new Exception(s"Resolution error: Union shape with missing anyof: $union")
    }

    union
  }
}
