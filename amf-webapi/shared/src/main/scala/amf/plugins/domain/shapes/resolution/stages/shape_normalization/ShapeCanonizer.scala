package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.annotations.{DeclaredElement, ExplicitField, LocalElement, ResolvedInheritance}
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.parser.Annotations
import amf.plugins.domain.shapes.annotations.InheritedShapes
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private[stages] object ShapeCanonizer {
  def apply(s: Shape, context: NormalizationContext): Shape = ShapeCanonizer()(context).normalize(s)
}

sealed case class ShapeCanonizer()(implicit val context: NormalizationContext) extends ShapeNormalizer {

  protected def cleanUnnecessarySyntax(shape: Shape): Shape = {
    shape.annotations.reject(a => !a.isInstanceOf[PerpetualAnnotation])
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
      case any: AnyShape             => canonicalAny(any)
    }
    if (!withoutCaching) context.cache + canonical // i should never add a shape if is not resolved yet
    context.cache.updateFixPointsAndClojures(canonical)

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

  private def canonicalAny(any: AnyShape) = {
    canonicalLogicalConstraints(any)
    if (any.inherits.nonEmpty) {
      canonicalInheritance(any)
    } else {
      AnyShapeAdjuster(any)
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
    if (endpointSimpleInheritance(shape)) {

      val referencedShape = shape.inherits.head
      aggregateExamples(shape, referencedShape)
      if (!referencedShape
            .isInstanceOf[RecursiveShape]) // i need to mark the reference shape as resolved to extract to declaration in graph emitter if is a declared element
        referencedShape.annotations += ResolvedInheritance()
      normalize(referencedShape)
    } else {
      val superTypes = shape.inherits
      val oldInherits: Seq[Shape] = if (context.keepEditingInfo) shape.inherits.collect {
        case rec: RecursiveShape => rec
        case shape: Shape        => shape.link(shape.name.value()).asInstanceOf[Shape]
      } else Nil
      shape.fields.removeField(ShapeModel.Inherits) // i need to remove the resolved type without inhertis, because later it will be added to cache once it will be fully resolved
      var accShape: Shape                             = normalizeWithoutCaching(shape)
      var superShapeswithDiscriminator: Seq[AnyShape] = Nil
      var inheritedIds: Seq[String]                   = Nil

      superTypes.foreach { superNode =>
        val canonicalSuperNode = normalizeAction(superNode)

        // we save this information to connect the references once we have computed the minShape
        if (hasDiscriminator(canonicalSuperNode))
          superShapeswithDiscriminator = superShapeswithDiscriminator ++ Seq(
            canonicalSuperNode.asInstanceOf[NodeShape])

        canonicalSuperNode match {
          case chain: InheritanceChain => inheritedIds ++= (Seq(canonicalSuperNode.id) ++ chain.inheritedIds)
          case _                       => inheritedIds :+= canonicalSuperNode.id
        }
        val newMinShape = context.minShape(accShape, canonicalSuperNode)
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

      // we set the full set of inherited IDs
      accShape match {
        case chain: InheritanceChain => chain.inheritedIds ++= inheritedIds
        case _                       => // ignore
      }

      accShape
    }
  }

  protected def aggregateExamples(shape: Shape, referencedShape: Shape) = {
    val names: mutable.Set[String] = mutable.Set() // duplicated names
    var exCounter                  = 0
    if (shape.isInstanceOf[AnyShape] && referencedShape.isInstanceOf[AnyShape]) {
      val accShape = shape.asInstanceOf[AnyShape]
      val refShape = referencedShape.asInstanceOf[AnyShape]
      accShape.examples.foreach { example =>
        val oldExamples = refShape.examples
        oldExamples.find(
          old =>
            old.id == example.id || old.raw
              .option()
              .getOrElse("")
              .trim == example.raw.option().getOrElse("").trim) match {
          case Some(_) => // duplicated
          case None =>
            example.annotations += LocalElement()
            refShape.setArrayWithoutId(AnyShapeModel.Examples, oldExamples ++ Seq(example))
        }
      }
      // we give proper names if there are more than one example, so it cannot be null
      if (refShape.examples.length > 1) {
        refShape.examples.foreach { example =>
          // we generate a unique new name if the no name or the name is already in the list of named examples
          if (example.name.option().isEmpty || names.contains(example.name.value())) {
            var name = s"example_$exCounter"
            while (names.contains(name)) {
              exCounter += 1
              name = s"example_$exCounter"
            }
            names.add(name)
            example.withName(name)
          } else {
            names.add(example.name.value())
          }
        }
      }
    }

  }

  def endpointSimpleInheritance(shape: Shape): Boolean = {
    shape match {
      case any: AnyShape if any.annotations.contains(classOf[DeclaredElement]) => false
      case any: AnyShape =>
        val singleInheritance = any.inherits.size == 1
        val effectiveFields = shape.fields.fields().filter { f =>
          f.field != ShapeModel.Inherits &&
          f.field != AnyShapeModel.Examples &&
          f.field != AnyShapeModel.Name
        //          f.field != AnyShapeModel.Name &&
//          f.field != ScalarShapeModel.DataType // ignore default datatype value
        }
        if (singleInheritance) {
          val superType = any.inherits.head
          effectiveFields.foldLeft(true) {
            case (acc, f) =>
              superType.fields.entry(f.field) match {
                case Some(e) if f.field == NodeShapeModel.Closed =>
                  acc && e.value.value.asInstanceOf[AmfScalar].toBool == f.value.value.asInstanceOf[AmfScalar].toBool
                case _ => f.field == NodeShapeModel.Closed && !superType.isInstanceOf[NodeShape]
              }
          }
        } else {
          false
        }
      case _ => false
    }
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
      union.anyOf.foreach { shape: Shape =>
        normalize(shape) match {
          case nestedUnion: UnionShape =>
            union.closureShapes ++= nestedUnion.closureShapes
            nestedUnion.anyOf.foreach(e => anyOfAcc += e)
          case rec: RecursiveShape =>
            rec.fixpointTarget.foreach(target => union.closureShapes ++= Seq(target).filter(_.id != union.id))
            anyOfAcc += rec
          case other: Shape =>
            union.closureShapes ++= other.closureShapes
            anyOfAcc += other
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
