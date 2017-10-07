package amf.resolution

import amf.document.BaseUnit
import amf.domain.Annotation.{ExplicitField, LexicalInformation}
import amf.domain.DomainElement
import amf.metadata.{MetaModelTypeMapping, Obj}
import amf.metadata.shape._
import amf.model.AmfArray
import amf.resolution.shape_normalization.MinShapeAlgorithm
import amf.shape._
import amf.vocabulary.{Namespace, ValueType}

import scala.collection.mutable.ListBuffer

/**
  * Computes the cannonical form for all the shapes in the model
  * We are assuming certain pre-conditions in the state of the shape:
  *  - All type references have been replaced by their expanded forms
  * @param profile
  * @return the resolved model
  */
class ShapeNormalizationStage(profile: String) extends ResolutionStage(profile) with MetaModelTypeMapping with MinShapeAlgorithm {

  val findShapesPredicate = (element: DomainElement) => {
    val metaModelFound: Obj = metaModel(element)
    val targetIri = (Namespace.Shapes + "Shape").iri()
    metaModelFound.`type`.exists { t: ValueType => t.iri() == targetIri }
  }

  override def resolve(model:BaseUnit, context: Any): BaseUnit = {
    model.transform(findShapesPredicate, transform)
  }

  protected def ensureCorrect(shape: Shape): Unit = {
    if (Option(shape.id).isEmpty) {
      throw new Exception(s"Resolution error: Found shape without ID: $shape")
    }
  }

  protected def cleanLexicalInfo(shape: Shape): Shape = {
    shape.annotations.reject(_.isInstanceOf[LexicalInformation])
    shape
  }

  protected def transform(element: DomainElement): Option[DomainElement] = element match {
    case shape: Shape => Some(canonical(expand(shape)))
    case other        => Some(other)
  }

  protected def expand(shape: Shape): Shape = {
    ensureCorrect(shape)
    cleanLexicalInfo(shape)
    shape match {
      case union: UnionShape       => expandUnion(union)
      case scalar: ScalarShape     => scalar
      case array: ArrayShape       => expandArray(array)
      case matrix: MatrixShape     => expandMatrix(matrix)
      case tuple: TupleShape       => expandTuple(tuple)
      case property: PropertyShape => expandProperty(property)
      case fileShape: FileShape    => fileShape
      case any: AnyShape           => any
      case nil: NilShape           => nil
      case node: NodeShape         => expandNode(node)
    }
  }

  protected def expandArray(array: ArrayShape): ArrayShape = {
    val oldItems = array.fields.getValue(ArrayShapeModel.Items)
    array.fields.setWithoutId(ArrayShapeModel.Items, expand(array.items), oldItems.annotations)
    array
  }

  protected def expandMatrix(matrix: MatrixShape): MatrixShape = {
    val oldItems = matrix.fields.getValue(MatrixShapeModel.Items)
    matrix.fields.setWithoutId(MatrixShapeModel.Items, expand(matrix.items), oldItems.annotations)
    matrix
  }

  protected def expandTuple(tuple: TupleShape): TupleShape = {
    val oldItems = tuple.fields.getValue(ArrayShapeModel.Items)
    val newItemShapes = tuple.items.map(shape => expand(shape))
    tuple.setArrayWithoutId(TupleShapeModel.Items, newItemShapes, oldItems.annotations)
    tuple
  }

  protected def expandNode(node: NodeShape): NodeShape = {
    val oldProperties = node.fields.getValue(NodeShapeModel.Properties)
    if (Option(oldProperties).isDefined) {
      val newProperties = node.properties.map(shape => expand(shape))
      node.setArrayWithoutId(NodeShapeModel.Properties, newProperties, oldProperties.annotations)
    }
    val oldInherits = node.fields.getValue(NodeShapeModel.Inherits)
    if (Option(oldInherits).isDefined) {
      val newInherits = node.inherits.map(shape => expand(shape))
      node.setArrayWithoutId(NodeShapeModel.Inherits, newInherits, oldInherits.annotations)
    }
    node
  }

  protected def expandProperty(property: PropertyShape): PropertyShape = {
    // property is mandatory
    if (Option(property.fields.getValue(PropertyShapeModel.MinCount)).isEmpty) {
      property.withMinCount(0)
    }

    val oldRange = property.fields.getValue(PropertyShapeModel.Range)
    if (Option(oldRange).isDefined) {
      property.fields.setWithoutId(PropertyShapeModel.Range, expand(property.range), oldRange.annotations)
    } else {
      throw new Exception(s"Resolution error: Property shape with missing range: $property")
    }

    property
  }

  protected def expandUnion(union: UnionShape): Shape = {
    val oldAnyOf = union.fields.getValue(UnionShapeModel.AnyOf)
    if (Option(oldAnyOf).isDefined) {
      val newAnyOf = union.anyOf.map(shape => expand(shape))
      union.setArrayWithoutId(UnionShapeModel.AnyOf, newAnyOf, oldAnyOf.annotations)
    } else {
      throw new Exception(s"Resolution error: Union shape with missing anyof: $union")
    }

    union
  }

  protected def canonical(shape: Shape): Shape = {
    shape match {
      case union: UnionShape       => canonicalUnion(union)
      case scalar: ScalarShape     => canonicalShape(scalar)
      case array: ArrayShape       => canonicalArray(array)
      case matrix: MatrixShape     => canonicalMatrix(matrix)
      case tuple: TupleShape       => canonicalTuple(tuple)
      case property: PropertyShape => canonicalProperty(property)
      case fileShape: FileShape    => canonicalShape(fileShape)
      case any: AnyShape           => canonicalShape(any)
      case nil: NilShape           => canonicalShape(nil)
      case node: NodeShape         => canonicalNode(node)
    }
  }


  protected def canonicalShape(shape: Shape) = shape

  protected def canonicalArray(array: ArrayShape): Shape = {
    if (Option(array.inherits).isDefined && array.inherits.nonEmpty) {
      val superTypes = array.inherits
      var accNode: Shape = canonical(array.withInherits(Seq()))
      superTypes.foreach { superNode =>
        accNode = canonical(minShape(accNode, canonical(superNode)))
      }
      accNode
    } else {
      val newItems = canonical(array.items)
      newItems match {
        case unionItems: UnionShape =>
          val newUnionItems = unionItems.anyOf.map(cloneShape(ArrayShape(), array).withItems(_))
          unionItems.setArrayWithoutId(UnionShapeModel.AnyOf, newUnionItems)
        case _ => array.withItems(newItems)
      }
    }
  }

  protected def canonicalMatrix(matrix: MatrixShape): Shape = {
    val newItems = canonical(matrix.items)
    newItems match {
      case unionItems: UnionShape =>
        val newUnionItems = unionItems.anyOf.map {
          case a: ArrayShape => cloneShape(MatrixShape(), matrix).withItems(a)
          case o => cloneShape(ArrayShape(), matrix.toArrayShape).withItems(o)
        }
        unionItems.setArrayWithoutId(UnionShapeModel.AnyOf, newUnionItems)
      case a: ArrayShape => matrix.withItems(a)
      case _             => matrix.toArrayShape.withItems(newItems)
    }
  }

  protected def canonicalNode(node: NodeShape): Shape = {
    node.add(ExplicitField())
    if (Option(node.inherits).isDefined && node.inherits.nonEmpty) {
      val superTypes = node.inherits
      var accNode: Shape = canonical(node.withInherits(Seq()))
      superTypes.foreach { superNode =>
        accNode = canonical(minShape(accNode, canonical(superNode)))
      }
      accNode
    } else {
      var acc = Seq(cloneShape(NodeShape(), node))
      node.properties.foreach { propertyShape =>
        canonical(propertyShape) match {
          case canonicalProperty: PropertyShape if canonicalProperty.range.isInstanceOf[UnionShape] =>
            val union = canonicalProperty.range.asInstanceOf[UnionShape]
            acc = for {
              unionElement <- union.anyOf
              accNode      <- acc
            } yield {
              val newProperties = accNode.properties.map { oldProperty =>
                if (oldProperty.path == propertyShape.path) {
                  val newProperty = cloneShape(PropertyShape(), canonicalProperty)
                  newProperty.fields.setWithoutId(PropertyShapeModel.Range, unionElement)
                  newProperty
                } else {
                  oldProperty
                }
              }
              cloneShape(NodeShape(), accNode).withProperties(newProperties)
            }
          case canonicalProperty: PropertyShape =>
            for {
              accNode <- acc
            } yield {
              val newProperties = accNode.properties.map { oldProperty =>
                if (oldProperty.path == propertyShape.path) {
                  canonicalProperty
                } else {
                  oldProperty
                }
              }
              cloneShape(NodeShape(), accNode).withProperties(newProperties)
            }
          case other => throw new Exception(s"Resolution error: Expecting property shape or union, found $other")
        }
      }
      if (acc.length == 1) {
        acc.head
      } else {
        UnionShape().withId(node.id + "resolved").setArrayWithoutId(UnionShapeModel.AnyOf, acc)
      }
    }
  }

  protected def canonicalProperty(property: PropertyShape): Shape = {
    property.fields.setWithoutId(PropertyShapeModel.Range, canonical(property.range), property.fields.getValue(PropertyShapeModel.Range).annotations)
    property
  }

  protected def canonicalUnion(union: UnionShape): Shape = {
    val anyOfAcc: ListBuffer[Shape] = ListBuffer()
    union.anyOf.foreach { shape =>
      canonical(shape) match {
        case union: UnionShape => union.anyOf.foreach(e => anyOfAcc += e)
        case other: Shape      => anyOfAcc += other
      }
    }
    union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(anyOfAcc), union.fields.getValue(UnionShapeModel.AnyOf).annotations)

    union
  }

  protected def canonicalTuple(tuple: TupleShape): Shape = {
    var acc: Seq[Seq[Shape]] = Seq(Seq())

    val sources: Seq[Seq[Shape]] = tuple.items.map { shape =>
      canonical(shape) match {
        case union: UnionShape => union.anyOf
        case other: Shape => Seq(other)
      }
    }

    sources.foreach { source =>
      source.foreach { shape =>
        acc = acc.map(_ ++ Seq(shape))
      }
    }

    if (acc.length == 1) {
      tuple.fields.setWithoutId(TupleShapeModel.Items, AmfArray(acc.head), tuple.fields.getValue(TupleShapeModel.Items).annotations)
      tuple
    } else {
      val tuples = acc.map { items =>
        val newTuple = cloneShape(TupleShape(), tuple)
        newTuple.fields.setWithoutId(TupleShapeModel.Items, AmfArray(items), tuple.fields.getValue(TupleShapeModel.Items).annotations)
      }
      val union = UnionShape()
      union.id = tuple.id + "resolved"
      union
    }
  }

  protected def cloneShape[T <: Shape](cloned: T, from: Shape): T = {
    from.fields.foreach {
      case (f,v) => cloned.fields.setWithoutId(f, v.value, v.annotations)
    }
    if (cloned.isInstanceOf[NodeShape]) {
      cloned.add(ExplicitField())
    }
    cloned
  }

}
