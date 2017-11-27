package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfArray, Shape}
import amf.core.parser.Annotations
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models._
import amf.core.vocabulary.Namespace

import scala.collection.mutable

trait MinShapeAlgorithm extends RestrictionComputation {

  protected def minShape(baseShapeOrig: Shape, superShape: Shape): Shape = {
    val baseShape = baseShapeOrig.cloneShape() // this is destructive, we need to clone
    baseShape match {

      // Scalars
      case baseScalar: ScalarShape if superShape.isInstanceOf[ScalarShape] =>
        val superScalar = superShape.asInstanceOf[ScalarShape]

        if (baseScalar.dataType == superScalar.dataType) {
          computeMinScalar(baseScalar, superScalar)
        } else if (baseScalar.dataType == (Namespace.Xsd + "integer")
                     .iri() && superScalar.dataType == (Namespace.Xsd + "float").iri()) {
          computeMinScalar(baseScalar, superScalar.withDataType((Namespace.Xsd + "integer").iri()))
        } else {
          throw new Exception(
            s"Resolution error: Invalid scalar inheritance base type ${baseScalar.dataType} < ${superScalar.dataType} ")
        }

      // Arrays
      case baseArray: ArrayShape if superShape.isInstanceOf[ArrayShape] =>
        val superArray = superShape.asInstanceOf[ArrayShape]
        computeMinArray(baseArray, superArray)
      case baseArray: MatrixShape if superShape.isInstanceOf[MatrixShape] =>
        val superArray = superShape.asInstanceOf[MatrixShape]
        computeMinMatrix(baseArray, superArray)
      case baseArray: TupleShape if superShape.isInstanceOf[TupleShape] =>
        val superArray = superShape.asInstanceOf[TupleShape]
        computeMinTuple(baseArray, superArray)

      // Nodes
      case baseNode: NodeShape if superShape.isInstanceOf[NodeShape] =>
        val superNode = superShape.asInstanceOf[NodeShape]
        computeMinNode(baseNode, superNode)

      // Unions
      case baseUnion: UnionShape if superShape.isInstanceOf[UnionShape] =>
        val superUnion = superShape.asInstanceOf[UnionShape]
        computeMinUnion(baseUnion, superUnion)
      case baseUnion: UnionShape if superShape.isInstanceOf[NodeShape] =>
        val superNode = superShape.asInstanceOf[NodeShape]
        computeMinUnionNode(baseUnion, superNode)

      // super Unions
      case base: Shape if superShape.isInstanceOf[UnionShape] =>
        val superUnion = superShape.asInstanceOf[UnionShape]
        computeMinSuperUnion(base, superUnion)

      case baseProperty: PropertyShape if superShape.isInstanceOf[PropertyShape] =>
        val superProperty = superShape.asInstanceOf[PropertyShape]
        computeMinProperty(baseProperty, superProperty)

      // Files
      case baseFile: FileShape if superShape.isInstanceOf[FileShape] =>
        val superFile = superShape.asInstanceOf[FileShape]
        computeMinFile(baseFile, superFile)

      // Nil
      case baseNil: NilShape if superShape.isInstanceOf[NilShape] => baseNil

      // Any => is explicitly Any, we are comparising the meta-model because now
        //      all shapes inherit from Any, cannot check with instanceOf
      case _ if baseShape.meta == AnyShapeModel || superShape.meta == AnyShapeModel =>
        baseShape match {
          case shape: AnyShape =>
            restrictShape(shape, superShape)
          case _ =>
            computeMinAny(baseShape, superShape.asInstanceOf[AnyShape])
        }

      // Generic inheritance
      case baseGeneric: NodeShape if isGenericNodeShape(baseGeneric) =>
        computeMinGeneric(baseGeneric, superShape)

      // fallback error
      case _ =>
        throw new Exception(s"Resolution error: Incompatible types [$baseShape, $superShape]")
    }
  }

  protected def isGenericNodeShape(shape: Shape) = {
    shape match {
      case node: NodeShape => Option(node.properties).getOrElse(Seq()).isEmpty
      case _               => false
    }
  }

  protected def computeMinScalar(baseScalar: ScalarShape, superScalar: ScalarShape): ScalarShape = {
    computeNarrowRestrictions(ScalarShapeModel.fields, baseScalar, superScalar)
    baseScalar
  }

  val allShapeFields =
    (ScalarShapeModel.fields ++ ArrayShapeModel.fields ++ NodeShapeModel.fields ++ AnyShapeModel.fields).distinct

  protected def computeMinAny(baseShape: Shape, anyShape: AnyShape): Shape = {
    computeNarrowRestrictions(allShapeFields, baseShape, anyShape)
    baseShape
  }

  protected def computeMinGeneric(baseShape: NodeShape, superShape: Shape) = restrictShape(baseShape, superShape)

  protected def computeMinMatrix(baseMatrix: MatrixShape, superMatrix: MatrixShape): Shape = {
    val superItems = baseMatrix.items
    val baseItems  = superMatrix.items

    val newItems = minShape(baseItems, superItems)
    baseMatrix.fields.setWithoutId(ArrayShapeModel.Items, newItems)

    computeNarrowRestrictions(ArrayShapeModel.fields,
                              baseMatrix,
                              superMatrix,
                              filteredFields = Seq(ArrayShapeModel.Items))

    baseMatrix
  }

  protected def computeMinTuple(baseTuple: TupleShape, superTuple: TupleShape): Shape = {
    val superItems = baseTuple.items
    val baseItems  = superTuple.items

    if (superItems.length != baseItems.length) {
      throw new Exception("Cannot inherit from a tuple shape with different number of elements")
    } else {
      val newItems = for {
        (baseItem, i) <- baseItems.view.zipWithIndex
      } yield {
        minShape(baseItem, superItems(i))
      }

      baseTuple.fields.setWithoutId(TupleShapeModel.Items,
                                    AmfArray(newItems),
                                    baseTuple.fields.get(TupleShapeModel.Items).annotations)

      computeNarrowRestrictions(TupleShapeModel.fields,
                                baseTuple,
                                superTuple,
                                filteredFields = Seq(TupleShapeModel.Items))

      baseTuple
    }
  }

  protected def computeMinArray(baseArray: ArrayShape, superArray: ArrayShape): Shape = {
    val superItems      = superArray.items
    val baseItemsOption = Option(baseArray.items)

    val newItems = baseItemsOption.fold(superItems)(baseItems => minShape(baseItems, superItems))
    baseArray.withItems(newItems)

    computeNarrowRestrictions(ArrayShapeModel.fields,
                              baseArray,
                              superArray,
                              filteredFields = Seq(ArrayShapeModel.Items))

    baseArray
  }

  protected def computeMinNode(baseNode: NodeShape, superNode: NodeShape): Shape = {
    val superProperties = superNode.properties
    val baseProperties  = baseNode.properties

    val commonProps: mutable.HashMap[String, Boolean] = mutable.HashMap()

    superProperties.foreach(p => commonProps.put(p.path, false))
    baseProperties.foreach { p =>
      if (commonProps.get(p.path).isDefined) {
        commonProps.put(p.path, true)
      } else {
        commonProps.put(p.path, false)
      }
    }

    val minProps = commonProps.map {
      case (path, true) =>
        val superProp = superProperties.find(_.path == path).get
        val baseProp  = baseProperties.find(_.path == path).get
        minShape(baseProp, superProp)
      case (path, false) =>
        val superProp = superProperties.find(_.path == path)
        val baseProp  = baseProperties.find(_.path == path)
        superProp.getOrElse(baseProp.get)
    }

    // This can be nil in the case of inheritance
    val annotations = Option(baseNode.fields.getValue(NodeShapeModel.Properties)) match {
      case Some(field) => field.annotations
      case None        => Annotations()
    }
    baseNode.fields.setWithoutId(NodeShapeModel.Properties, AmfArray(minProps.toSeq), annotations)

    computeNarrowRestrictions(NodeShapeModel.fields,
                              baseNode,
                              superNode,
                              filteredFields = Seq(NodeShapeModel.Properties))

    baseNode
  }

  protected def computeMinUnion(baseUnion: UnionShape, superUnion: UnionShape): Shape = {
    val newUnionItems = for {
      baseUnionElement  <- baseUnion.anyOf
      superUnionElement <- superUnion.anyOf
    } yield {
      minShape(baseUnionElement, superUnionElement)
    }

    baseUnion.fields.setWithoutId(UnionShapeModel.AnyOf,
                                  AmfArray(newUnionItems),
                                  baseUnion.fields.getValue(UnionShapeModel.AnyOf).annotations)

    computeNarrowRestrictions(UnionShapeModel.fields,
                              baseUnion,
                              superUnion,
                              filteredFields = Seq(UnionShapeModel.AnyOf))

    baseUnion
  }

  protected def computeMinUnionNode(baseUnion: UnionShape, superNode: NodeShape): Shape = {
    val newUnionItems = for {
      baseUnionElement <- baseUnion.anyOf
    } yield {
      minShape(baseUnionElement, superNode)
    }

    baseUnion.fields.setWithoutId(UnionShapeModel.AnyOf,
                                  AmfArray(newUnionItems),
                                  baseUnion.fields.getValue(UnionShapeModel.AnyOf).annotations)

    computeNarrowRestrictions(UnionShapeModel.fields,
                              baseUnion,
                              superNode,
                              filteredFields = Seq(UnionShapeModel.AnyOf))

    baseUnion
  }

  protected def computeMinSuperUnion(baseShape: Shape, superUnion: UnionShape): Shape = {
    val newUnionItems = for {
      superUnionElement <- superUnion.anyOf
    } yield {
      minShape(baseShape, superUnionElement)
    }

    superUnion.fields.setWithoutId(UnionShapeModel.AnyOf,
                                   AmfArray(newUnionItems),
                                   superUnion.fields.getValue(UnionShapeModel.AnyOf).annotations)

    computeNarrowRestrictions(allShapeFields, baseShape, superUnion, filteredFields = Seq(UnionShapeModel.AnyOf))
    baseShape.fields foreach {
      case (field, value) =>
        if (field != UnionShapeModel.AnyOf) {
          superUnion.fields.setWithoutId(field, value.value, value.annotations)
        }
    }

    superUnion
  }

  def computeMinProperty(baseProperty: PropertyShape, superProperty: PropertyShape): Shape = {
    val newRange = minShape(baseProperty.range, superProperty.range)

    baseProperty.fields.setWithoutId(PropertyShapeModel.Range,
                                     newRange,
                                     baseProperty.fields.getValue(PropertyShapeModel.Range).annotations)

    computeNarrowRestrictions(PropertyShapeModel.fields,
                              baseProperty,
                              superProperty,
                              filteredFields = Seq(PropertyShapeModel.Range))

    baseProperty
  }

  def computeMinFile(baseFile: FileShape, superFile: FileShape): Shape = {
    computeNarrowRestrictions(FileShapeModel.fields, baseFile, superFile)
    baseFile
  }

}
