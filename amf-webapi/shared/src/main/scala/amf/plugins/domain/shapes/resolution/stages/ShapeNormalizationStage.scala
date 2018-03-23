package amf.plugins.domain.shapes.resolution.stages

import amf.ProfileNames
import amf.core.annotations.ExplicitField
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.metamodel.{MetaModelTypeMapping, Obj}
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Annotations
import amf.core.parser.Position.ZERO
import amf.core.resolution.stages.ResolutionStage
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.resolution.stages.shape_normalization.MinShapeAlgorithm
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo

import scala.collection.mutable.ListBuffer

/**
  * Computes the canonical form for all the shapes in the model
  * We are assuming certain pre-conditions in the state of the shape:
  *  - All type references have been replaced by their expanded forms
  * @param profile
  */
class ShapeNormalizationStage(profile: String, val keepEditingInfo: Boolean)
    extends ResolutionStage(profile)
    with MetaModelTypeMapping
    with MinShapeAlgorithm {

  var fixPointCount = 0

  override def resolve(model: BaseUnit): BaseUnit =
     profile match {
      case ProfileNames.RAML08 => model
      case _                   => model.transform(findShapesPredicate, transform)
    }

  protected def ensureCorrect(shape: Shape): Unit = {
    if (Option(shape.id).isEmpty) {
      throw new Exception(s"Resolution error: Found shape without ID: $shape")
    }
  }

  protected def cleanUnnecessarySyntax(shape: Shape): Shape = {
    shape.annotations.reject(!_.isInstanceOf[PerpetualAnnotation])
    // 'type' properties are explicit
    if (shape.annotations.find(classOf[TypePropertyLexicalInfo]).isEmpty)
      shape.annotations += TypePropertyLexicalInfo(amf.core.parser.Range(ZERO, ZERO))

    shape
  }

  def findShapesPredicate(element: DomainElement) = {
    val metaModelFound: Obj = metaModel(element)
    val targetIri           = (Namespace.Shapes + "Shape").iri()
    metaModelFound.`type`.exists { t: ValueType =>
      t.iri() == targetIri
    }
  }

  protected def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    element match {
      case shape: Shape => Some(canonical(expand(shape.cloneShape(Some(element.id)))))
      case other        => Some(other)
    }
  }

  protected def expand(shape: Shape): Shape = {
    ensureCorrect(shape)
    cleanUnnecessarySyntax(shape)
    shape match {
      case union: UnionShape         => expandUnion(union)
      case scalar: ScalarShape       => expandScalar(scalar)
      case array: ArrayShape         => expandArray(array)
      case matrix: MatrixShape       => expandMatrix(matrix)
      case tuple: TupleShape         => expandTuple(tuple)
      case property: PropertyShape   => expandProperty(property)
      case fileShape: FileShape      => fileShape
      case nil: NilShape             => nil
      case node: NodeShape           => expandNode(node)
      case recursive: RecursiveShape => recursive
      case any: AnyShape             => any
    }
  }

  protected def expandInherits(shape: Shape): Unit = {
    val oldInherits = shape.fields.getValue(ShapeModel.Inherits)
    if (Option(oldInherits).isDefined) {
      val newInherits = shape.inherits.map(expand)
      shape.setArrayWithoutId(ShapeModel.Inherits, newInherits, oldInherits.annotations)
    }
  }

  protected def expandScalar(scalar: ScalarShape): ScalarShape = {
    expandInherits(scalar)
    scalar
  }

  protected def expandArray(array: ArrayShape): ArrayShape = {
    expandInherits(array)
    val oldItems = array.fields.getValue(ArrayShapeModel.Items)
    if (Option(oldItems).isDefined || (Option(oldItems).isEmpty && array.inherits.isEmpty)) // we check the items, but if this shape inherits from another array with items, this is not mandatory locally
      array.fields.setWithoutId(ArrayShapeModel.Items, expand(array.items), oldItems.annotations)
    array
  }

  protected def expandMatrix(matrix: MatrixShape): MatrixShape = {
    val oldItems = matrix.fields.getValue(MatrixShapeModel.Items)
    matrix.fields.setWithoutId(MatrixShapeModel.Items, expand(matrix.items), oldItems.annotations)
    matrix
  }

  protected def expandTuple(tuple: TupleShape): TupleShape = {
    val oldItems      = tuple.fields.getValue(ArrayShapeModel.Items)
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

    expandInherits(node)

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
      val expandedRange = expand(property.range)
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
      val newAnyOf = union.anyOf.map(shape => expand(shape))
      union.setArrayWithoutId(UnionShapeModel.AnyOf, newAnyOf, oldAnyOf.annotations)
    } else if (Option(union.inherits).isEmpty || union.inherits.isEmpty) {
      throw new Exception(s"Resolution error: Union shape with missing anyof: $union")
    }

    union
  }

  protected def canonical(shape: Shape): Shape = {
    shape match {
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
  }

  private def canonicalShape(any: Shape) = {
    if (any.inherits.nonEmpty) {
      canonicalInheritance(any)
    } else {
      any
    }
  }

  protected def canonicalScalar(scalar: ScalarShape): Shape = {
    if (Option(scalar.inherits).isDefined && scalar.inherits.nonEmpty) {
      canonicalInheritance(scalar)
    } else {
      scalar
    }
  }

  protected def canonicalInheritance(shape: Shape): Shape = {
    val superTypes = shape.inherits
    if (!keepEditingInfo) shape.fields.remove(ShapeModel.Inherits)
    var accShape: Shape = canonical(shape)
    superTypes.foreach { superNode =>
      val canonicalSuperNode = canonical(superNode)
      val newMinShape        = minShape(accShape, canonicalSuperNode)
      accShape = canonical(newMinShape)
    }
    accShape
  }

  protected def canonicalArray(array: ArrayShape): Shape = {
    if (array.inherits.nonEmpty) {
      canonicalInheritance(array)
    } else {
      Option(array.items).fold(array.asInstanceOf[Shape])(i => {
        val newItems = canonical(i)
        array.annotations += ExplicitField()
        array.fields.remove(ArrayShapeModel.Items)
        newItems match {
          case unionItems: UnionShape =>
            // The canonical items is a union, we need to push it to the top
            // array[items: Union(a,b,c)] ==> Union(array[items:a], array[items:b], array[items:c])
            val newUnionItems = unionItems.anyOf.map { item =>
              val newArray = array.cloneShape().withItems(item)
              newArray.annotations += ExplicitField()
              newArray
            }
            unionItems.setArrayWithoutId(UnionShapeModel.AnyOf, newUnionItems)
            Option(array.fields.getValue(ShapeModel.Name)) match {
              case Some(name) => unionItems.withName(name.toString)
              case _          => unionItems
            }
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
    val newItems = canonical(matrix.items)
    matrix.fields.remove(ArrayShapeModel.Items)
    newItems match {
      case unionItems: UnionShape =>
        val newUnionItems = unionItems.anyOf.map {
          case a: ArrayShape => matrix.cloneShape().withItems(a)
          case o             => matrix.cloneShape().toArrayShape.withItems(o)
        }
        unionItems.setArrayWithoutId(UnionShapeModel.AnyOf, newUnionItems)
        Option(matrix.fields.getValue(ShapeModel.Name)) match {
          case Some(name) => unionItems.withName(name.toString)
          case _          => unionItems
        }
      case a: ArrayShape => matrix.withItems(a)
      case _             => matrix.toArrayShape.withItems(newItems)
    }
  }

  protected def canonicalNode(node: NodeShape): Shape = {
    node.add(ExplicitField())
    if (node.inherits.nonEmpty) {
      canonicalInheritance(node)
    } else {
      // We start processing the properties by cloning the base node shape
      val canonicalProperties: Seq[PropertyShape] = node.properties.map { propertyShape =>
        canonical(propertyShape) match {
          case canonicalProperty: PropertyShape => canonicalProperty
          case other                            => throw new Exception(s"Resolution error: Expecting property shape, found $other")
        }
      }
      node.withProperties(canonicalProperties)
    }
  }

  protected def canonicalProperty(property: PropertyShape): Shape = {
    property.fields.setWithoutId(PropertyShapeModel.Range,
                                 canonical(property.range),
                                 property.fields.getValue(PropertyShapeModel.Range).annotations)
    property
  }

  protected def canonicalUnion(union: UnionShape): Shape = {
    if (union.inherits.nonEmpty) {
      canonicalInheritance(union)
    } else {
      val anyOfAcc: ListBuffer[Shape] = ListBuffer()
      union.anyOf.foreach { shape =>
        canonical(shape) match {
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

  protected def canonicalTuple(tuple: TupleShape): Shape = {
    var acc: Seq[Seq[Shape]] = Seq(Seq())

    val sources: Seq[Seq[Shape]] = tuple.items.map { shape =>
      canonical(shape) match {
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
      tuple.fields.setWithoutId(TupleShapeModel.Items,
                                AmfArray(acc.head),
                                tuple.fields.getValue(TupleShapeModel.Items).annotations)
      tuple
    } else {
      val tuples = acc.map { items =>
        val newTuple = tuple.cloneShape()
        newTuple.fields.setWithoutId(TupleShapeModel.Items,
                                     AmfArray(items),
                                     tuple.fields.getValue(TupleShapeModel.Items).annotations)
      }
      val union = UnionShape()
      union.id = tuple.id + "resolved"
      union.withName(tuple.name.value())
      union
    }
  }

}
