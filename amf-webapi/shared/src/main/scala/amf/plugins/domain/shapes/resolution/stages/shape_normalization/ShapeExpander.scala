package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.annotations.{ExplicitField, LexicalInformation}
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Annotations
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models._
import amf.plugins.features.validation.ParserSideValidations

private[stages] object ShapeExpander {
  def apply(s: Shape, context: NormalizationContext): Shape = new ShapeExpander(s)(context).normalize()
}

sealed case class ShapeExpander(root: Shape)(implicit val context: NormalizationContext) extends ShapeNormalizer {

  def normalize(): Shape = normalize(root)

  protected val traversed: IdsTraversionCheck =
    IdsTraversionCheck().withAllowedCyclesInstances(Seq(classOf[UnresolvedShape]))

  private def buildRecursion(base: Option[String], s: Shape): RecursiveShape = {
    val fixPointId = base.getOrElse(s.id)
    RecursiveShape(s).withFixPoint(fixPointId)
  }

  private def recursionAndError(base: Option[String], s: Shape): RecursiveShape =
    recursionError(root, buildRecursion(base, s))

  private def recursionError(original: Shape, r: RecursiveShape): RecursiveShape = {
    if (!r.supportsRecursion
          .option()
          .getOrElse(false) && !traversed.avoidError(original.id)) // todo should store in recursion it use to
      context.errorHandler.violation(
        ParserSideValidations.RecursiveShapeSpecification.id(),
        original.id,
        None,
        "Error recursive shape",
        original.annotations.find(classOf[LexicalInformation])
      )
    r
  }

  protected def ensureCorrect(shape: Shape): Unit = {
    if (Option(shape.id).isEmpty) {
      throw new Exception(s"Resolution error: Found shape without ID: $shape")
    }
  }

  private def recursiveNormalization(shape: Shape) = traversed.runPushed(_ => normalize(shape))

  override def normalizeAction(shape: Shape): Shape = {
    shape match {
      case l: Linkable if l.isLink                                          => recursionAndError(Some(root.id), shape)
      case _ if traversed.has(shape) && !shape.isInstanceOf[RecursiveShape] => recursionAndError(None, shape)
      case _ =>
        ensureCorrect(shape)
        traversed + shape.id
        traversed.runPushed(_ => {
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
            case recursive: RecursiveShape => recursive
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
        case r: RecursiveShape => recursionError(shape, r)
        case other             => recursiveNormalization(other)
      }
      shape.setArrayWithoutId(ShapeModel.Inherits, newInherits, oldInherits.annotations)
    }
  }

  protected def expandLogicalConstraints(shape: Shape): Unit = {
    var oldLogicalConstraints = shape.fields.getValue(ShapeModel.And)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.and.map { recursiveNormalization }
      shape.setArrayWithoutId(ShapeModel.And, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Or)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.or.map { recursiveNormalization }
      shape.setArrayWithoutId(ShapeModel.Or, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Xone)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.xone.map { recursiveNormalization }
      shape.setArrayWithoutId(ShapeModel.Xone, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    val notConstraint = shape.fields.getValue(ShapeModel.Not)
    if (Option(notConstraint).isDefined) {
      val newLogicalConstraint = recursiveNormalization(shape.not)
      shape.set(ShapeModel.Not, newLogicalConstraint, notConstraint.annotations)
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
    val oldItems = array.fields.getValue(ArrayShapeModel.Items)
    if (Option(oldItems).isDefined)
      array.fields.setWithoutId(ArrayShapeModel.Items, recursiveNormalization(array.items), oldItems.annotations)
    array
  }

  protected def expandMatrix(matrix: MatrixShape): MatrixShape = {
    expandLogicalConstraints(matrix)
    val oldItems = matrix.fields.getValue(MatrixShapeModel.Items)
    if (Option(oldItems).isDefined)
      matrix.fields.setWithoutId(MatrixShapeModel.Items, recursiveNormalization(matrix.items), oldItems.annotations)
    matrix
  }

  protected def expandTuple(tuple: TupleShape): TupleShape = {
    expandLogicalConstraints(tuple)
    val oldItems = tuple.fields.getValue(TupleShapeModel.TupleItems)
    if (Option(oldItems).isDefined) {
      val newItemShapes = tuple.items.map { recursiveNormalization }
      tuple.setArrayWithoutId(TupleShapeModel.TupleItems, newItemShapes, oldItems.annotations)
    }
    tuple
  }

  protected def expandNode(node: NodeShape): NodeShape = {
    val oldProperties = node.fields.getValue(NodeShapeModel.Properties)
    if (Option(oldProperties).isDefined) {
      val newProperties = node.properties.map { recursiveNormalization }
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
      case None => throw new Exception("MinCount field is mandatory in a shape")
      case Some(entry) =>
        if (entry.value.value.asInstanceOf[AmfScalar].toNumber.intValue() != 0) {
          required = true
        }
    }

    val oldRange = property.fields.getValue(PropertyShapeModel.Range)
    if (Option(oldRange).isDefined) {
//      val expandedRange = recursiveNormalization(property.range)
      val expandedRange =
        if (!required) traverseOptionalPropertyRange(property.range) else recursiveNormalization(property.range)
      // Making the required property explicit
      checkRequiredShape(expandedRange, required)
      expandedRange.fields
        .entry(ShapeModel.RequiredShape)
        .foreach(f =>
          if (f.value.annotations.contains(classOf[ExplicitField]))
            property.fields.entry(PropertyShapeModel.MinCount).foreach(f => f.value.annotations.+=(ExplicitField())))

      property.fields.setWithoutId(PropertyShapeModel.Range, expandedRange, oldRange.annotations)
    } else {
      throw new Exception(s"Resolution error: Property shape with missing range: $property")
    }
    property
  }

  private def traverseOptionalPropertyRange(range: Shape) = {
    range.linkTarget match {
      case Some(t) => traversed.runWithIgnoredId(() => normalize(range), t.id)
      case None if range.inherits.nonEmpty =>
        traversed.runWithIgnoredIds(() => normalize(range), range.inherits.map(_.id))
      case _ => traversed.runWithIgnoredId(() => normalize(range), range.id)
    }
  }

  protected def checkRequiredShape(shape: Shape, required: Boolean): Unit = {
    Option(shape.fields.getValue(ShapeModel.RequiredShape)) match {
      case Some(v) => v.annotations += ExplicitField()
      case None =>
        shape.fields.setWithoutId(ShapeModel.RequiredShape, AmfScalar(required), Annotations() += ExplicitField())
    }
  }

  protected def expandUnion(union: UnionShape): Shape = {
    expandInherits(union)
    val oldAnyOf = union.fields.getValue(UnionShapeModel.AnyOf)
    if (Option(oldAnyOf).isDefined) {
      val newAnyOf = union.anyOf.map { recursiveNormalization }
      union.setArrayWithoutId(UnionShapeModel.AnyOf, newAnyOf, oldAnyOf.annotations)
    } else if (Option(union.inherits).isEmpty || union.inherits.isEmpty) {
      throw new Exception(s"Resolution error: Union shape with missing anyof: $union")
    }

    union
  }
}
