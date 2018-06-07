package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.annotations.ExplicitField
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfArray, PerpetualAnnotation, RecursiveShape, Shape}
import amf.core.parser.Annotations
import amf.plugins.domain.shapes.annotations.InheritedShapes
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, NodeShapeModel, TupleShapeModel, UnionShapeModel}
import amf.plugins.domain.shapes.models._

import scala.collection.mutable.ListBuffer

private[stages] object ShapeCanonizer {
  def apply(s: Shape, context: NormalizationContext): Shape = ShapeCanonizer()(context).normalize(s)
}

sealed case class ShapeCanonizer()(implicit val context: NormalizationContext) extends ShapeNormalizer {

  protected def cleanUnnecessarySyntax(shape: Shape): Shape = {
    shape.annotations.reject(!_.isInstanceOf[PerpetualAnnotation])
    shape
  }

  private var withoutCaching = false

  private def runWithoutCaching[T](fn: () => T): T = {
    withoutCaching = true
    val t: T = fn()
    withoutCaching = false
    t
  }

  private def normalizeWithoutCaching(s: Shape): Shape = runWithoutCaching(() => normalize(s))

  private def actionWithoutCaching(s: Shape): Shape = runWithoutCaching(() => normalizeAction(s))

  override protected def normalizeAction(shape: Shape): Shape = {
    cleanUnnecessarySyntax(shape)
    val canonical = shape match {
      case union: UnionShape         => canonicalUnion(union)
      case scalar: ScalarShape       => canonicalScalar(scalar)
      case array: ArrayShape         => canonicalArray(array)
      case matrix: MatrixShape       => canonicalMatrix(matrix)
      case tuple: TupleShape         => canonicalTuple(tuple)
      case property: PropertyShape   => canonicalProperty(property)
      case fileShape: FileShape      => canonicalShape(fileShape)
      case nil: NilShape             => canonicalShape(nil)
      case node: NodeShape           => canonicalNode(node)
      case recursive: RecursiveShape => recursive
      case any: AnyShape             => canonicalShape(any)
    }
    if (!withoutCaching) context.cache + canonical // i should never add a shape if is not resolved yet
    canonical
  }

  protected def canonicalLogicalConstraints(shape: Shape): Unit = {
    var oldLogicalConstraints = shape.fields.getValue(ShapeModel.And)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.and.map(normalize)
      shape.setArrayWithoutId(ShapeModel.And, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Or)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.or.map(normalize)
      shape.setArrayWithoutId(ShapeModel.Or, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Xone)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.xone.map(normalize)
      shape.setArrayWithoutId(ShapeModel.Xone, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    val notConstraint = shape.fields.getValue(ShapeModel.Not)
    if (Option(notConstraint).isDefined) {
      val newLogicalConstraint = normalize(shape.not)
      shape.set(ShapeModel.Not, newLogicalConstraint, notConstraint.annotations)
    }
  }

  private def canonicalShape(any: Shape) = {
    canonicalLogicalConstraints(any)
    if (any.inherits.nonEmpty) {
      canonicalInheritance(any)
    } else {
      any
    }
  }

  protected def canonicalScalar(scalar: ScalarShape): Shape = {
    canonicalLogicalConstraints(scalar)
    if (Option(scalar.inherits).isDefined && scalar.inherits.nonEmpty) {
      canonicalInheritance(scalar)
    } else {
      scalar
    }
  }

  protected def canonicalInheritance(shape: Shape): Shape = {
    val superTypes = shape.inherits
    val oldInherits: Seq[Shape] = if (context.keepEditingInfo) shape.inherits.collect {
      case rec: RecursiveShape => rec
      case shape: Shape        => shape.link(shape.name.value()).asInstanceOf[Shape]
    } else Nil
    shape.fields.removeField(ShapeModel.Inherits) // i need to remove the resolved type without inhertis, because later it will be added to cache once it will be fully resolved
    var accShape: Shape = normalizeWithoutCaching(shape)
    var superShapeswithDiscriminator: Seq[AnyShape] = Nil
    superTypes.foreach { superNode =>
      val canonicalSuperNode = normalizeAction(superNode)

      // we save this information to connect the references once we have computed the minShape
      if (hasDiscriminator(canonicalSuperNode)) superShapeswithDiscriminator = superShapeswithDiscriminator ++ Seq(canonicalSuperNode.asInstanceOf[NodeShape])

      val newMinShape        = context.minShape(accShape, canonicalSuperNode)
      accShape = actionWithoutCaching(newMinShape)
    }
    if (context.keepEditingInfo) accShape.annotations += InheritedShapes(oldInherits.map(_.id))
    if (!shape.id.equals(accShape.id)) {
      context.cache.registerMapping(shape.id, accShape.id)
      accShape.withId(shape.id) // i need to override id, if not i will override the father catched shape
    }

    // adjust inheritance chain if discriminator is defined
    accShape match {
      case any: AnyShape => superShapeswithDiscriminator.foreach(_.linkSubType(any))
      case _             => // ignore
    }

    accShape
  }

  protected def hasDiscriminator(shape: Shape): Boolean = {
    shape match {
      case anyShape: NodeShape => anyShape.discriminator.option().isDefined
      case _                   => false
    }
  }

  protected def canonicalArray(array: ArrayShape): Shape = {
    canonicalLogicalConstraints(array)
    if (array.inherits.nonEmpty) {
      canonicalInheritance(array)
    } else {
      Option(array.items).fold(array.asInstanceOf[Shape])(i => {
        val newItems = normalize(i)
        array.annotations += ExplicitField()
        array.fields.removeField(ArrayShapeModel.Items)
        newItems match {
          case arrayItems: ArrayShape =>
            // Array items -> array must become a Matrix
            array.fields.setWithoutId(ArrayShapeModel.Items, newItems)
            array.toMatrixShape
          case _ =>
            // No union, we just set the new canonical items
            array.fields.setWithoutId(ArrayShapeModel.Items, newItems)
            array
        }
      })
    }
  }

  protected def canonicalMatrix(matrix: MatrixShape): Shape = {
    canonicalLogicalConstraints(matrix)
    if (matrix.inherits.nonEmpty) {
      canonicalInheritance(matrix)
    } else {
      Option(matrix.items) match {
        case Some(items) =>
          val newItems = normalize(items)
          matrix.fields.removeField(ArrayShapeModel.Items)
          newItems match {
            case unionItems: UnionShape =>
              val newUnionItems = unionItems.anyOf.map {
                case a: ArrayShape => matrix.cloneShape(Some(context.errorHandler)).withItems(a)
                case o             => matrix.cloneShape(Some(context.errorHandler)).toArrayShape.withItems(o)
              }
              unionItems.setArrayWithoutId(UnionShapeModel.AnyOf, newUnionItems)
              Option(matrix.fields.getValue(ShapeModel.Name)) match {
                case Some(name) => unionItems.withName(name.toString)
                case _          => unionItems
              }
            case a: ArrayShape => matrix.withItems(a)
            case _             => matrix.toArrayShape.withItems(newItems)
          }
        case _ => matrix
      }
    }
  }

  protected def canonicalTuple(tuple: TupleShape): Shape = {
    canonicalLogicalConstraints(tuple)
    if (tuple.inherits.nonEmpty) {
      canonicalInheritance(tuple)
    } else {
      var acc: Seq[Seq[Shape]] = Seq(Seq())

      val sources: Seq[Seq[Shape]] = tuple.items.map { shape =>
        normalize(shape) match {
          case union: UnionShape => union.anyOf
          case other: Shape      => Seq(other)
        }
      }

      sources.foreach { source =>
        source.foreach { shape =>
          acc = acc.map(_ ++ Seq(shape))
        }
      }

      if (acc.length == 1) {
        tuple.fields.setWithoutId(
          TupleShapeModel.TupleItems,
          AmfArray(acc.head),
          Option(tuple.fields.getValue(TupleShapeModel.TupleItems)).map(_.annotations).getOrElse(Annotations()))
        tuple
      } else {
        val tuples = acc.map { items =>
          val newTuple = tuple.cloneShape(Some(context.errorHandler))
          newTuple.fields.setWithoutId(
            TupleShapeModel.Items,
            AmfArray(items),
            Option(tuple.fields.getValue(TupleShapeModel.Items)).map(_.annotations).getOrElse(Annotations()))
        }
        val union = UnionShape()
        union.id = tuple.id + "resolved"
        union.withName(tuple.name.value())
        union
      }
    }
  }

  protected def canonicalNode(node: NodeShape): Shape = {
    canonicalLogicalConstraints(node)
    node.add(ExplicitField())
    if (node.inherits.nonEmpty) {
      canonicalInheritance(node)
    } else {
      // We start processing the properties by cloning the base node shape
      val canonicalProperties: Seq[PropertyShape] = node.properties.map { propertyShape =>
        normalize(propertyShape) match {
          case canonicalProperty: PropertyShape => canonicalProperty
          case other                            => throw new Exception(s"Resolution error: Expecting property shape, found $other")
        }
      }
      node.setArrayWithoutId(NodeShapeModel.Properties, canonicalProperties)

    }
  }

  protected def canonicalProperty(property: PropertyShape): Shape = {
    property.fields.setWithoutId(PropertyShapeModel.Range,
                                 normalize(property.range),
                                 property.fields.getValue(PropertyShapeModel.Range).annotations)
    property
  }

  protected def canonicalUnion(union: UnionShape): Shape = {
    if (union.inherits.nonEmpty) {
      canonicalInheritance(union)
    } else {
      val anyOfAcc: ListBuffer[Shape] = ListBuffer()
      union.anyOf.foreach { shape =>
        normalize(shape) match {
          case union: UnionShape => union.anyOf.foreach(e => anyOfAcc += e)
          case other: Shape      => anyOfAcc += other
        }
      }
      val anyOfAnnotations = Option(union.fields.getValue(UnionShapeModel.AnyOf)) match {
        case Some(anyOf) => anyOf.annotations
        case _           => Annotations()
      }

      union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(anyOfAcc), anyOfAnnotations)

      union
    }
  }
}
