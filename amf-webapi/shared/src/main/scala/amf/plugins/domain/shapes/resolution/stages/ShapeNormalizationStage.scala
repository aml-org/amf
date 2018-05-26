package amf.plugins.domain.shapes.resolution.stages

import amf.core.annotations.ExplicitField
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.metamodel.{MetaModelTypeMapping, Obj}
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.{Annotations, ErrorHandler}
import amf.core.resolution.stages.ResolutionStage
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.annotations.InheritedShapes
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.resolution.stages.shape_normalization.MinShapeAlgorithm

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Computes the canonical form for all the shapes in the model
  * We are assuming certain pre-conditions in the state of the shape:
  *  - All type references have been replaced by their expanded forms
  * @param profile
  */
class ShapeNormalizationStage(profile: String, val keepEditingInfo: Boolean, val errorHandler: ErrorHandler)
    extends ResolutionStage(profile)
    with MetaModelTypeMapping
    with MinShapeAlgorithm {

  var fixPointCount = 0

  private val cache = NormalizationCache()

  private case class NormalizationCache() {
    def registerMapping(id: String, alias: String): this.type = {
      mappings.get(alias) match {
        case Some(a) =>
          mappings.remove(alias)
          mappings.put(id, a)
        case _ =>
          mappings.put(id, alias)
          fixPointCache.get(id).foreach { seq =>
            fixPointCache.remove(id)
            fixPointCache.put(alias, seq.map(_.withFixPoint(alias)))
          }
      }

      this
    }

    private val cache         = mutable.Map[String, Shape]()
    private val fixPointCache = mutable.Map[String, Seq[RecursiveShape]]()
    private val mappings      = mutable.Map[String, String]()

    private def registerFixPoint(r: RecursiveShape): RecursiveShape = {
      r.fixpoint.option().foreach { fp =>
        val alias = mappings.get(fp)
        fixPointCache.get(fp) match {
          case Some(s) =>
            val shapes = s :+ r
            val newAlias = alias.fold({ fp })(a => {
              shapes.foreach(_.withFixPoint(a))
              fixPointCache.remove(fp)
              a
            })
            fixPointCache.put(newAlias, shapes)

          case _ =>
            alias.fold({ fixPointCache.put(fp, Seq(r)) })(a => {
              r.withFixPoint(a)
              fixPointCache.put(a, Seq(r))
            })
        }
      }
      r
    }

    def +(shape: Shape): this.type = {
      shape match {
        case r: RecursiveShape =>
          registerFixPoint(r)
        case _ =>
      }
      cache.put(shape.id, shape)
      this
    }

    def get(id: String): Option[Shape] = {

      cache.get(id).map(_.cloneShape(Some(errorHandler), Some(id))) match {
        case Some(r: RecursiveShape) =>
          if (r.fixpoint
                .value()
                .equals(
                  "file://amf-client/shared/src/test/resources/production/recursive-union.raml#/declarations/any/SomeType"))
            println("here")
          Some(registerFixPoint(r))
        case other => other
      }
    }

    def getUncloned(id: String): Option[Shape] = cache.get(id)

    def exists(id: String): Boolean = cache.contains(id)
  }

  override def resolve(model: BaseUnit): BaseUnit = model.transform(findShapesPredicate, transform)

  protected def ensureCorrect(shape: Shape): Unit = {
    if (Option(shape.id).isEmpty) {
      throw new Exception(s"Resolution error: Found shape without ID: $shape")
    }
  }

  protected def cleanUnnecessarySyntax(shape: Shape): Shape = {
    shape.annotations.reject(!_.isInstanceOf[PerpetualAnnotation])
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
      // need to copy before enter the expand, to avoid copy in cycle, but i should be good idea check if its declared or not, toavoid copy without be necessary
      case shape: Shape if cache.exists(shape.id) => Some(canonical(expand(shape)))
      case shape: Shape                           => Some(canonical(expand(shape.cloneShape(Some(errorHandler), Some(element.id)))))
      case other                                  => Some(other)
    }
  }

  protected def expand(shape: Shape): Shape = {
    cache.get(shape.id) match {
      case Some(s) => s
      case _ =>
        ensureCorrect(shape)
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
    }
  }

  protected def expandInherits(shape: Shape): Unit = {
    val oldInherits = shape.fields.getValue(ShapeModel.Inherits)
    if (Option(oldInherits).isDefined) {
      val newInherits = shape.inherits.map(expand)
      shape.setArrayWithoutId(ShapeModel.Inherits, newInherits, oldInherits.annotations)
    }
  }

  protected def expandLogicalConstraints(shape: Shape): Unit = {
    var oldLogicalConstraints = shape.fields.getValue(ShapeModel.And)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.and.map(expand)
      shape.setArrayWithoutId(ShapeModel.And, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Or)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.or.map(expand)
      shape.setArrayWithoutId(ShapeModel.Or, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Xone)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.xone.map(expand)
      shape.setArrayWithoutId(ShapeModel.Xone, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    val notConstraint = shape.fields.getValue(ShapeModel.Not)
    if (Option(notConstraint).isDefined) {
      val newLogicalConstraint = expand(shape.not)
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
      array.fields.setWithoutId(ArrayShapeModel.Items, expand(array.items), oldItems.annotations)
    array
  }

  protected def expandMatrix(matrix: MatrixShape): MatrixShape = {
    expandLogicalConstraints(matrix)
    val oldItems = matrix.fields.getValue(MatrixShapeModel.Items)
    if (Option(oldItems).isDefined)
      matrix.fields.setWithoutId(MatrixShapeModel.Items, expand(matrix.items), oldItems.annotations)
    matrix
  }

  protected def expandTuple(tuple: TupleShape): TupleShape = {
    expandLogicalConstraints(tuple)
    val oldItems = tuple.fields.getValue(TupleShapeModel.TupleItems)
    if (Option(oldItems).isDefined) {
      val newItemShapes = tuple.items.map(shape => expand(shape))
      tuple.setArrayWithoutId(TupleShapeModel.TupleItems, newItemShapes, oldItems.annotations)
    }
    tuple
  }

  protected def expandNode(node: NodeShape): NodeShape = {
    val oldProperties = node.fields.getValue(NodeShapeModel.Properties)
    if (Option(oldProperties).isDefined) {
      val newProperties = node.properties.map(shape => expand(shape))
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
    // i need to get the shape from cache. maybe this instance was added resolving some type and not the current instance (the instance may be not resolved yet)
    cache.getUncloned(shape.id) match {
      case Some(s) if !s.getClass.equals(shape.getClass) =>
        s.cloneShape(Some(errorHandler), Some(s.id)) // if are diff clases, i should get the cached one
      case Some(s) if s.fields.size != s.fields.size =>
        s.cloneShape(Some(errorHandler), Some(s.id)) // if are of the same class, but has diff fields size(should check fields keys??) i used the cached one
      case Some(s) =>
        shape // if are of the same class and have the same fields i assume that shape has been copied from cache and i can return that instance
      // todo: some way to check the cached shape if it's equals to actual shape to avoid the unnesary re copy?
      case _ =>
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
        cache + canonical //i should never add a shape if is not resolved yet
        canonical
    }
  }

  protected def canonicalLogicalConstraints(shape: Shape): Unit = {
    var oldLogicalConstraints = shape.fields.getValue(ShapeModel.And)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.and.map(canonical)
      shape.setArrayWithoutId(ShapeModel.And, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Or)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.or.map(canonical)
      shape.setArrayWithoutId(ShapeModel.Or, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    oldLogicalConstraints = shape.fields.getValue(ShapeModel.Xone)
    if (Option(oldLogicalConstraints).isDefined) {
      val newLogicalConstraints = shape.xone.map(canonical)
      shape.setArrayWithoutId(ShapeModel.Xone, newLogicalConstraints, oldLogicalConstraints.annotations)
    }

    val notConstraint = shape.fields.getValue(ShapeModel.Not)
    if (Option(notConstraint).isDefined) {
      val newLogicalConstraint = canonical(shape.not)
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
    val oldInherits: Seq[Shape] = if (keepEditingInfo) shape.inherits.collect {
      case rec: RecursiveShape => rec
      case shape: Shape        => shape.link(shape.name.value()).asInstanceOf[Shape]
    } else Nil
    shape.fields.removeField(ShapeModel.Inherits)
    var accShape: Shape = canonical(shape)
    superTypes.foreach { superNode =>
      val canonicalSuperNode = canonical(superNode)
      val newMinShape        = minShape(accShape, canonicalSuperNode)
      accShape = canonical(newMinShape)
    }
    if (keepEditingInfo) accShape.annotations += InheritedShapes(oldInherits.map(_.id))
    if (!shape.id.equals(accShape.id)) {
      cache.registerMapping(shape.id, accShape.id)
      accShape.withId(shape.id)
    }
    accShape
  }

  protected def canonicalArray(array: ArrayShape): Shape = {
    canonicalLogicalConstraints(array)
    if (array.inherits.nonEmpty) {
      canonicalInheritance(array)
    } else {
      Option(array.items).fold(array.asInstanceOf[Shape])(i => {
        val newItems = canonical(i)
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
    val newItems = canonical(matrix.items)
    matrix.fields.removeField(ArrayShapeModel.Items)
    newItems match {
      case unionItems: UnionShape =>
        val newUnionItems = unionItems.anyOf.map {
          case a: ArrayShape => matrix.cloneShape(Some(errorHandler)).withItems(a)
          case o             => matrix.cloneShape(Some(errorHandler)).toArrayShape.withItems(o)
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

  protected def canonicalTuple(tuple: TupleShape): Shape = {
    canonicalLogicalConstraints(tuple)
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
      tuple.fields.setWithoutId(TupleShapeModel.TupleItems,
                                AmfArray(acc.head),
                                tuple.fields.getValue(TupleShapeModel.TupleItems).annotations)
      tuple
    } else {
      val tuples = acc.map { items =>
        val newTuple = tuple.cloneShape(Some(errorHandler))
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

  protected def canonicalNode(node: NodeShape): Shape = {
    canonicalLogicalConstraints(node)
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
      node.setArrayWithoutId(NodeShapeModel.Properties, canonicalProperties)

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
}
