package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.annotations.LexicalInformation
import amf.core.metamodel.Field
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{AmfArray, RecursiveShape, Shape}
import amf.core.parser.{Annotations, ErrorHandler, Value}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.annotations.ParsedJSONSchema
import amf.plugins.domain.shapes.annotations.InheritanceProvenance
import amf.plugins.domain.shapes.metamodel._
import amf.plugins.domain.shapes.models._
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.YError

import scala.collection.mutable

class InheritanceIncompatibleShapeError(val message: String,
                                        val property: Option[String] = None,
                                        val lexicalInfo: Option[LexicalInformation] = None)
    extends Exception(message)

private[stages] class MinShapeAlgorithm()(implicit val context: NormalizationContext) extends RestrictionComputation {

  // this is inverted, it is safe because recursive shape does not have facets
  def computeMinRecursive(baseShape: Shape, recursiveShape: RecursiveShape): Shape = {
    restrictShape(baseShape, recursiveShape)
  }

  private def copy(shape: Shape) = {
    shape match {
      case a: AnyShape => a.copyShape()
      case _           => shape
    }
  }

  def computeMinShape(derivedShapeOrig: Shape, superShapeOri: Shape): Shape = {
    val superShape   = copy(superShapeOri)
    val derivedShape = derivedShapeOrig.cloneShape(Some(context.errorHandler)) // this is destructive, we need to clone
    derivedShape.closureShapes ++= superShape.closureShapes
//    context.cache.updateRecursiveTargets(derivedShape)
    try {
      derivedShape match {

        // Scalars
        case baseScalar: ScalarShape if superShape.isInstanceOf[ScalarShape] =>
          val superScalar = superShape.asInstanceOf[ScalarShape]

          val b = baseScalar.dataType.value()
          val s = superScalar.dataType.value()
          if (b == s) {
            computeMinScalar(baseScalar, superScalar)
          } else if (b == (Namespace.Xsd + "integer").iri() &&
                     (s == (Namespace.Xsd + "float").iri() ||
                     s == (Namespace.Xsd + "double").iri() ||
                     s == (Namespace.Shapes + "number").iri())) {
            computeMinScalar(baseScalar, superScalar.withDataType((Namespace.Xsd + "integer").iri()))
          } else {
            context.errorHandler.violation(
              ParserSideValidations.InvalidTypeInheritanceErrorSpecification.id,
              derivedShape,
              Some(ShapeModel.Inherits.value.iri()),
              s"Resolution error: Invalid scalar inheritance base type $b < $s "
            )
            baseScalar
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

        // Generic inheritance
        case _ if superShape.isInstanceOf[RecursiveShape] =>
          computeMinRecursive(derivedShape, superShape.asInstanceOf[RecursiveShape])

        // Any => is explicitly Any, we are comparing the meta-model because now
        //      all shapes inherit from Any, cannot check with instanceOf
        case _ if derivedShape.meta == AnyShapeModel || superShape.meta == AnyShapeModel =>
          derivedShape match {
            case shape: AnyShape =>
              restrictShape(shape, superShape)
            case _ =>
              computeMinAny(derivedShape, superShape.asInstanceOf[AnyShape])
          }

        // Generic inheritance
        case baseGeneric: NodeShape if isGenericNodeShape(baseGeneric) && superShape.isInstanceOf[NodeShape] =>
          computeMinGeneric(baseGeneric, superShape)

        case schema: SchemaShape if superShape.meta == SchemaShapeModel =>
          computeMinSchema(superShape, schema)
        // fallback error
        case _ =>
          context.errorHandler.violation(
            ParserSideValidations.InvalidTypeInheritanceErrorSpecification.id,
            derivedShape,
            Some(ShapeModel.Inherits.value.iri()),
            s"Resolution error: Incompatible types [${derivedShape.getClass}, ${superShape.getClass}]"
          )
          derivedShape
      }
    } catch {
      case e: InheritanceIncompatibleShapeError =>
        context.errorHandler.warning(
          ParserSideValidations.InvalidTypeInheritanceWarningSpecification.id,
          derivedShape.id,
          e.property.orElse(Some(ShapeModel.Inherits.value.iri())),
          e.getMessage,
          e.lexicalInfo,
          derivedShape.position().map(_.value)
        )
        derivedShape
    }
  }

  private def computeMinSchema(superShape: Shape, schema: SchemaShape) = {
    superShape.fields
      .foreach({
        case (f: Field, v: Value) if !schema.fields.exists(f) =>
          schema.set(f, v.value, v.annotations)
        case _ =>
      })
    schema
  }

  protected def isGenericNodeShape(shape: Shape) = {
    shape match {
      case node: NodeShape => node.properties.isEmpty
      case _               => false
    }
  }

  protected def computeMinScalar(baseScalar: ScalarShape, superScalar: ScalarShape): ScalarShape = {
    computeNarrowRestrictions(ScalarShapeModel.fields, baseScalar, superScalar)
    baseScalar
  }

  private val allShapeFields =
    (ScalarShapeModel.fields ++ ArrayShapeModel.fields ++ NodeShapeModel.fields ++ AnyShapeModel.fields).distinct

  protected def computeMinAny(baseShape: Shape, anyShape: AnyShape): Shape = {
    computeNarrowRestrictions(allShapeFields, baseShape, anyShape)
    baseShape
  }

  protected def computeMinGeneric(baseShape: NodeShape, superShape: Shape) = restrictShape(baseShape, superShape)

  protected def computeMinMatrix(baseMatrix: MatrixShape, superMatrix: MatrixShape): Shape = {

    val superItems = superMatrix.items
    val baseItems  = baseMatrix.items
    if (Option(superItems).isDefined && Option(baseItems).isDefined) {

      val newItems = context.minShape(baseItems, superItems)
      baseMatrix.fields.setWithoutId(ArrayShapeModel.Items, newItems)

      computeNarrowRestrictions(ArrayShapeModel.fields,
                                baseMatrix,
                                superMatrix,
                                filteredFields = Seq(ArrayShapeModel.Items))
    } else {
      if (Option(superItems).isDefined) baseMatrix.fields.setWithoutId(ArrayShapeModel.Items, superItems)
    }

    baseMatrix
  }

  protected def computeMinTuple(baseTuple: TupleShape, superTuple: TupleShape): Shape = {
    val superItems = baseTuple.items
    val baseItems  = superTuple.items

    if (superItems.length != baseItems.length) {
      if (context.isRaml08 && baseItems.isEmpty) {
        baseTuple.fields.setWithoutId(TupleShapeModel.Items,
                                      AmfArray(superItems),
                                      baseTuple.fields.get(TupleShapeModel.Items).annotations)
        baseTuple
      } else {
        throw new InheritanceIncompatibleShapeError(
          "Cannot inherit from a tuple shape with different number of elements",
          None,
          baseTuple.position())
      }
    } else {
      val newItems = for {
        (baseItem, i) <- baseItems.view.zipWithIndex
      } yield {
        context.minShape(baseItem, superItems(i))
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
    val superItemsOption = Option(superArray.items)
    val baseItemsOption  = Option(baseArray.items)

    val newItems = baseItemsOption
      .map { baseItems =>
        superItemsOption match {
          case Some(superItems) => context.minShape(baseItems, superItems)
          case _                => baseItems
        }
      }
      .orElse(superItemsOption)
    newItems.foreach { ni =>
      baseArray.withItems(ni)
    }

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

    superProperties.foreach(p => commonProps.put(p.path.value(), false))
    baseProperties.foreach { p =>
      if (commonProps.get(p.path.value()).isDefined) {
        commonProps.put(p.path.value(), true)
      } else {
        commonProps.put(p.path.value(), false)
      }
    }

    val minProps = commonProps.map {
      case (path, true) =>
        val superProp = superProperties.find(_.path.is(path)).get
        val baseProp  = baseProperties.find(_.path.is(path)).get
        context.minShape(baseProp, superProp)
      case (path, false) =>
        val superProp = superProperties.find(_.path.is(path))
        val baseProp  = baseProperties.find(_.path.is(path))
        if (keepEditingInfo) {
          superProp.map(inheritProp(superNode)).getOrElse { baseProp.get.cloneShape(Some(context.errorHandler)) }
        } else {
          superProp.map(_.cloneShape(Some(context.errorHandler))).getOrElse {
            baseProp.get.cloneShape(Some(context.errorHandler))
          }
        }
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
                              filteredFields = Seq(NodeShapeModel.Properties, NodeShapeModel.Examples))

    // if its raml 08 i need to keep parsed json schema annotation in order to emit a valid nodeshape.
    // Remember that objects in 08 are only valid in external schemas or as formProperties under only two media types (form undercoder and formData)
    if (context.isRaml08)
      superNode.annotations.find(classOf[ParsedJSONSchema]).foreach { baseNode.annotations += _ }

    baseNode
  }

  def inheritProp(from: Shape)(prop: PropertyShape): PropertyShape = {
    val clonedProp = prop.cloneShape(Some(context.errorHandler))
    if (clonedProp.annotations.find(classOf[InheritanceProvenance]).isEmpty)
      clonedProp.annotations += InheritanceProvenance(from.id)
    clonedProp
  }

  val unionErrorHandler = new ErrorHandler {
    override val parserCount: Int    = 0
    override val currentFile: String = ""

    override def handle[T](error: YError, defaultValue: T): T = {
      throw new Exception("raising exceptions in union processing")
    }

    override protected def reportConstraint(id: String,
                                            node: String,
                                            property: Option[String],
                                            message: String,
                                            lexical: Option[LexicalInformation],
                                            level: String,
                                            location: Option[String]): Unit = {
      throw new Exception("raising exceptions in union processing")
    }

    def wrapContext(ctx: NormalizationContext) = {
      new NormalizationContext(
        this,
        ctx.keepEditingInfo,
        ctx.profile,
        ctx.cache
      )
    }
  }
  protected def computeMinUnion(baseUnion: UnionShape, superUnion: UnionShape): Shape = {

    val unionContext: NormalizationContext = unionErrorHandler.wrapContext(context)
    val newUnionItems =
      if (baseUnion.anyOf.isEmpty || superUnion.anyOf.isEmpty) {
        baseUnion.anyOf ++ superUnion.anyOf
      } else {
        val minShapes = for {
          baseUnionElement  <- baseUnion.anyOf
          superUnionElement <- superUnion.anyOf
        } yield {
          try {
            Some(unionContext.minShape(baseUnionElement, superUnionElement))
          } catch {
            case _: Exception => None
          }
        }
        val finalMinShapes = minShapes.collect { case Some(s) => s }
        if (finalMinShapes.isEmpty)
          throw new InheritanceIncompatibleShapeError("Cannot compute inheritance for union",
                                                      None,
                                                      baseUnion.position())
        finalMinShapes
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
    val unionContext: NormalizationContext = unionErrorHandler.wrapContext(context)
    val newUnionItems = for {
      baseUnionElement <- baseUnion.anyOf
    } yield {
      unionContext.minShape(baseUnionElement, superNode)
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
    val unionContext: NormalizationContext = unionErrorHandler.wrapContext(context)
    val minItems = for {
      superUnionElement <- superUnion.anyOf
    } yield {
      try {
        Some(unionContext.minShape(baseShape, superUnionElement))
      } catch {
        case _: Exception => None
      }
    }
    val newUnionItems = minItems collect { case Some(s) => s }
    if (newUnionItems.isEmpty) {
      throw new InheritanceIncompatibleShapeError("Cannot compute inheritance from union", None, baseShape.position())
    }

    var accExamples = List[Example]()

    val unionShapesWithIds = newUnionItems.zipWithIndex.map {
      case (shape, i) =>
        shape.id = shape.id + s"_$i"
        shape match {
          case any: AnyShape =>
            accExamples ++= any.examples
            any.fields.removeField(AnyShapeModel.Examples)
          case _ => // ignore
        }
        shape
    }

    superUnion.fields.setWithoutId(UnionShapeModel.AnyOf,
                                   AmfArray(unionShapesWithIds),
                                   superUnion.fields.getValue(UnionShapeModel.AnyOf).annotations)

    computeNarrowRestrictions(allShapeFields, baseShape, superUnion, filteredFields = Seq(UnionShapeModel.AnyOf))
    baseShape.fields foreach {
      case (field, value) =>
        if (field != UnionShapeModel.AnyOf) {
          superUnion.fields.setWithoutId(field, value.value, value.annotations)
        }
    }

    if (accExamples.nonEmpty)
      superUnion.fields.setWithoutId(AnyShapeModel.Examples, AmfArray(accExamples.distinct))

    superUnion
  }

  def computeMinProperty(baseProperty: PropertyShape, superProperty: PropertyShape): Shape = {
    val newRange = context.minShape(baseProperty.range, superProperty.range)

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

  override val keepEditingInfo: Boolean = context.keepEditingInfo
}
