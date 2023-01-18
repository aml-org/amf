package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.platform.model.domain.CustomDomainProperty
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfScalar, RecursiveShape, ScalarNode, Shape}
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.annotations.{Inferred, InheritanceProvenance, LexicalInformation}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.{Annotations, Value}
import amf.core.internal.utils.IdCounter
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.internal.domain.metamodel._
import amf.shapes.internal.spec.RamlShapeTypeBeautifier
import amf.shapes.internal.validation.definitions.ShapeResolutionSideValidations.{InvalidTypeInheritanceErrorSpecification, InvalidTypeInheritanceWarningSpecification}

import scala.collection.mutable

class InheritanceIncompatibleShapeError(
    val message: String,
    val property: Option[String] = None,
    val location: Option[String] = None,
    val position: Option[LexicalInformation] = None,
    val isViolation: Boolean = false
) extends Exception(message)

private[resolution] class MinShapeAlgorithm()(implicit val context: NormalizationContext) {

  protected def computeNarrowRestrictions(
                                           fields: Seq[Field],
                                           baseShape: Shape,
                                           superShape: Shape,
                                           filteredFields: Seq[Field] = Seq.empty
                                         ): Shape = {
    fields.foreach { f =>
      if (!filteredFields.contains(f)) {
        val baseValue = Option(baseShape.fields.getValue(f))
        val superValue = Option(superShape.fields.getValue(f))
        baseValue match {
          case Some(bvalue) if superValue.isEmpty => baseShape.set(f, bvalue.value, bvalue.annotations)

          case None if superValue.isDefined =>
            val finalAnnotations = Annotations(superValue.get.annotations)
            if (keepEditingInfo) inheritAnnotations(finalAnnotations, superShape)
            baseShape.fields.setWithoutId(f, superValue.get.value, finalAnnotations)

          case Some(bvalue) if superValue.isDefined =>
            val finalAnnotations = Annotations(bvalue.annotations)
            val finalValue = computeNarrow(f, bvalue.value, superValue.get.value)
            if (finalValue != bvalue.value && keepEditingInfo) inheritAnnotations(finalAnnotations, superShape)
            val effective = finalValue.add(finalAnnotations)
            baseShape.fields.setWithoutId(f, effective, finalAnnotations)
          case _ => // ignore
        }
      }
    }

    baseShape
  }

  def inheritAnnotations(annotations: Annotations, from: Shape) = {
    if (!annotations.contains(classOf[InheritanceProvenance]))
      annotations += InheritanceProvenance(from.id)
    annotations
  }

  protected def restrictShape(restriction: Shape, shape: Shape): Shape = {
    shape.id = restriction.id
    restriction.fields.foreach { case (field, derivedValue) =>
      if (field != NodeShapeModel.Inherits) {
        Option(shape.fields.getValue(field)) match {
          case Some(superValue) => shape.set(field, computeNarrow(field, derivedValue.value, superValue.value))
          case None => shape.fields.setWithoutId(field, derivedValue.value, derivedValue.annotations)
        }
      }
    }
    shape
  }

  protected def computeNumericRestriction(
                                           comparison: String,
                                           lvalue: AmfElement,
                                           rvalue: AmfElement,
                                           property: Option[String] = None,
                                           lexicalInfo: Option[LexicalInformation] = None
                                         ): AmfElement = {
    lvalue match {
      case scalar: AmfScalar
        if Option(scalar.value).isDefined && rvalue
          .isInstanceOf[AmfScalar] && Option(rvalue.asInstanceOf[AmfScalar].value).isDefined =>
        val lnum = scalar.toNumber
        val rnum = rvalue.asInstanceOf[AmfScalar].toNumber

        comparison match {
          case "max" =>
            if (lnum.intValue() <= rnum.intValue()) {
              rvalue
            } else {
              lvalue
            }
          case "min" =>
            if (lnum.intValue() >= rnum.intValue()) {
              rvalue
            } else {
              lvalue
            }
          case _ => throw new InheritanceIncompatibleShapeError(s"Unknown numeric comparison $comparison")
        }
      case _ =>
        throw new InheritanceIncompatibleShapeError("Cannot compare non numeric or missing values")
    }
  }

  protected def computeEnum(
                             derivedEnumeration: Seq[AmfElement],
                             superEnumeration: Seq[AmfElement],
                             annotations: Annotations
                           ): Unit = {
    if (derivedEnumeration.nonEmpty && superEnumeration.nonEmpty) {
      val headOption = derivedEnumeration.headOption
      if (headOption.exists(h => superEnumeration.headOption.exists(_.getClass != h.getClass)))
        throw new InheritanceIncompatibleShapeError(
          s"Values in subtype enumeration are from different class '${derivedEnumeration.head.getClass}' of the super type enumeration '${superEnumeration.head.getClass}'",
          Some(ShapeModel.Values.value.iri()),
          headOption.flatMap(_.location()),
          headOption.flatMap(_.position())
        )

      derivedEnumeration match {
        case Seq(_: ScalarNode) =>
          val superScalars = superEnumeration.collect({ case s: ScalarNode => s.value.value() })
          val ds = derivedEnumeration.asInstanceOf[Seq[ScalarNode]]
          ds.foreach { e =>
            if (!superScalars.contains(e.value.value())) {
              throw new InheritanceIncompatibleShapeError(
                s"Values in subtype enumeration (${ds.map(_.value).mkString(",")}) not found in the supertype enumeration (${
                  superScalars
                    .mkString(",")
                })",
                Some(ShapeModel.Values.value.iri()),
                e.location(),
                e.position()
              )
            }
          }
        case _ => // ignore
      }
    }
  }

  protected def computeStringEquality(
                                       lvalue: AmfElement,
                                       rvalue: AmfElement,
                                       property: Option[String] = None,
                                       position: Option[String],
                                       lexicalInfo: Option[LexicalInformation] = None
                                     ): Boolean = {
    lvalue match {
      case scalar: AmfScalar
        if Option(scalar.value).isDefined && rvalue
          .isInstanceOf[AmfScalar] && Option(rvalue.asInstanceOf[AmfScalar].value).isDefined =>
        val lstr = scalar.toString
        val rstr = rvalue.asInstanceOf[AmfScalar].toString
        lstr == rstr
      case _ =>
        throw new InheritanceIncompatibleShapeError(
          "Cannot compare non numeric or missing values",
          property,
          position,
          lexicalInfo
        )
    }
  }

  protected def stringValue(value: AmfElement): Option[String] = {
    value match {
      case scalar: AmfScalar
        if Option(scalar.value).isDefined && value
          .isInstanceOf[AmfScalar] && Option(value.asInstanceOf[AmfScalar].value).isDefined =>
        Some(scalar.toString)
      case _ => None
    }
  }

  protected def computeNumericComparison(
                                          comparison: String,
                                          lvalue: AmfElement,
                                          rvalue: AmfElement,
                                          property: Option[String] = None,
                                          lexicalInformation: Option[LexicalInformation] = None,
                                          location: Option[String]
                                        ): Boolean = {
    lvalue match {
      case scalar: AmfScalar
        if Option(scalar.value).isDefined && rvalue
          .isInstanceOf[AmfScalar] && Option(rvalue.asInstanceOf[AmfScalar].value).isDefined =>
        val lnum = scalar.toNumber
        val rnum = rvalue.asInstanceOf[AmfScalar].toNumber

        comparison match {
          case "<=" =>
            lnum.intValue() <= rnum.intValue()
          case ">=" =>
            lnum.intValue() >= rnum.intValue()
          case _ =>
            throw new InheritanceIncompatibleShapeError(
              s"Unknown numeric comparison $comparison",
              property,
              location,
              lexicalInformation
            )
        }
      case _ =>
        throw new InheritanceIncompatibleShapeError(
          "Cannot compare non numeric or missing values",
          property,
          location,
          lexicalInformation
        )
    }
  }

  protected def computeBooleanComparison(
                                          lcomparison: Boolean,
                                          rcomparison: Boolean,
                                          lvalue: AmfElement,
                                          rvalue: AmfElement,
                                          property: Option[String] = None,
                                          lexicalInformaiton: Option[LexicalInformation] = None
                                        ): Boolean = {
    lvalue match {
      case scalar: AmfScalar
        if Option(scalar.value).isDefined && rvalue
          .isInstanceOf[AmfScalar] && Option(rvalue.asInstanceOf[AmfScalar].value).isDefined =>
        val lbool = scalar.toBool
        val rbool = rvalue.asInstanceOf[AmfScalar].toBool
        lbool == lcomparison && rbool == rcomparison
      case _ =>
        throw new InheritanceIncompatibleShapeError("Cannot compare non boolean or missing values")
    }
  }

  protected def computeNarrow(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    field match {

      case ShapeModel.Name =>
        val derivedStrValue = stringValue(derivedValue)
        val superStrValue = stringValue(superValue)
        if (superStrValue.isDefined && (derivedStrValue.isEmpty || derivedStrValue.get == "schema")) {
          superValue
        } else {
          derivedValue
        }

      case NodeShapeModel.MinProperties =>
        if (
          computeNumericComparison(
            "<=",
            superValue,
            derivedValue,
            Some(NodeShapeModel.MinProperties.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "max",
            superValue,
            derivedValue,
            Some(NodeShapeModel.MinProperties.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for min-properties than base type for minProperties",
            Some(NodeShapeModel.MinProperties.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case NodeShapeModel.MaxProperties =>
        if (
          computeNumericComparison(
            ">=",
            superValue,
            derivedValue,
            Some(NodeShapeModel.MaxProperties.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "min",
            superValue,
            derivedValue,
            Some(NodeShapeModel.MaxProperties.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for max-properties than base type for maxProperties",
            Some(NodeShapeModel.MaxProperties.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case ScalarShapeModel.MinLength =>
        if (
          computeNumericComparison(
            "<=",
            superValue,
            derivedValue,
            Some(ScalarShapeModel.MinLength.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "max",
            superValue,
            derivedValue,
            Some(ScalarShapeModel.MinLength.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for min-length than base type for maxProperties",
            Some(ScalarShapeModel.MinLength.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case ScalarShapeModel.MaxLength =>
        if (
          computeNumericComparison(
            ">=",
            superValue,
            derivedValue,
            Some(ScalarShapeModel.MaxLength.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "min",
            superValue,
            derivedValue,
            Some(ScalarShapeModel.MaxLength.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for max-length than base type for maxProperties",
            Some(ScalarShapeModel.MaxLength.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case ScalarShapeModel.Minimum =>
        if (
          computeNumericComparison(
            "<=",
            superValue,
            derivedValue,
            Some(ScalarShapeModel.Minimum.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "max",
            superValue,
            derivedValue,
            Some(ScalarShapeModel.Minimum.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for min-minimum than base type for minimum",
            Some(ScalarShapeModel.Minimum.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case ScalarShapeModel.Maximum =>
        if (
          computeNumericComparison(
            ">=",
            superValue,
            derivedValue,
            Some(ScalarShapeModel.Maximum.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "min",
            superValue,
            derivedValue,
            Some(ScalarShapeModel.Maximum.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for maximum than base type for maximum",
            Some(ScalarShapeModel.Maximum.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case ArrayShapeModel.MinItems =>
        if (
          computeNumericComparison(
            "<=",
            superValue,
            derivedValue,
            Some(ArrayShapeModel.MinItems.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "max",
            superValue,
            derivedValue,
            Some(ArrayShapeModel.MinItems.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for minItems than base type for minItems",
            Some(ArrayShapeModel.MinItems.value.iri()),
            derivedValue.location(),
            derivedValue.position(),
            isViolation = true
          )
        }

      case ArrayShapeModel.MaxItems =>
        if (
          computeNumericComparison(
            ">=",
            superValue,
            derivedValue,
            Some(ArrayShapeModel.MaxItems.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "min",
            superValue,
            derivedValue,
            Some(ArrayShapeModel.MaxItems.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for maxItems than base type for maxItems",
            Some(ArrayShapeModel.MaxItems.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case ScalarShapeModel.Format =>
        if (
          computeStringEquality(
            superValue,
            derivedValue,
            Some(ScalarShapeModel.Format.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        ) {
          derivedValue
        } else {
          throw new InheritanceIncompatibleShapeError(
            "different values for format constraint",
            Some(ScalarShapeModel.Format.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case ScalarShapeModel.Pattern =>
        if (
          computeStringEquality(
            superValue,
            derivedValue,
            Some(ScalarShapeModel.Pattern.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        ) {
          derivedValue
        } else {
          throw new InheritanceIncompatibleShapeError(
            "different values for pattern constraint",
            Some(ScalarShapeModel.Pattern.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case NodeShapeModel.Discriminator =>
        if (
          !computeStringEquality(
            superValue,
            derivedValue,
            Some(NodeShapeModel.Discriminator.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        ) {
          derivedValue
        } else {
          throw new InheritanceIncompatibleShapeError(
            "shape has same discriminator value as parent",
            Some(NodeShapeModel.Discriminator.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case NodeShapeModel.DiscriminatorValue =>
        if (
          !computeStringEquality(
            superValue,
            derivedValue,
            Some(NodeShapeModel.DiscriminatorValue.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        ) {
          derivedValue
        } else {
          throw new InheritanceIncompatibleShapeError(
            "shape has same discriminator value as parent",
            Some(NodeShapeModel.DiscriminatorValue.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case ShapeModel.Values =>
        computeEnum(
          derivedValue.asInstanceOf[AmfArray].values,
          superValue.asInstanceOf[AmfArray].values,
          derivedValue.annotations
        )
        derivedValue

      case ArrayShapeModel.UniqueItems =>
        if (
          computeBooleanComparison(
            lcomparison = true,
            rcomparison = true,
            superValue,
            derivedValue,
            Some(ArrayShapeModel.UniqueItems.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          ) ||
            computeBooleanComparison(
              lcomparison = false,
              rcomparison = false,
              superValue,
              derivedValue,
              Some(ArrayShapeModel.UniqueItems.value.iri()),
              derivedValue.annotations.find(classOf[LexicalInformation])
            ) ||
            computeBooleanComparison(
              lcomparison = false,
              rcomparison = true,
              superValue,
              derivedValue,
              Some(ArrayShapeModel.UniqueItems.value.iri()),
              derivedValue.annotations.find(classOf[LexicalInformation])
            )
        ) {
          derivedValue
        } else {
          throw new InheritanceIncompatibleShapeError(
            "different values for unique items constraint",
            Some(ArrayShapeModel.UniqueItems.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case PropertyShapeModel.MinCount =>
        if (
          computeNumericComparison(
            "<=",
            superValue,
            derivedValue,
            Some(PropertyShapeModel.MinCount.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "max",
            superValue,
            derivedValue,
            Some(PropertyShapeModel.MinCount.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for minCount than base type for minCount",
            Some(PropertyShapeModel.MinCount.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case PropertyShapeModel.MaxCount =>
        if (
          computeNumericComparison(
            ">=",
            superValue,
            derivedValue,
            Some(PropertyShapeModel.MaxCount.value.iri()),
            derivedValue.position(),
            derivedValue.location()
          )
        ) {
          computeNumericRestriction(
            "min",
            superValue,
            derivedValue,
            Some(PropertyShapeModel.MaxCount.value.iri()),
            derivedValue.annotations.find(classOf[LexicalInformation])
          )
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for maxCount than base type for maxCount",
            Some(PropertyShapeModel.MaxCount.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case PropertyShapeModel.Path =>
        if (
          computeStringEquality(
            superValue,
            derivedValue,
            Some(PropertyShapeModel.Path.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        ) {
          derivedValue
        } else {
          throw new InheritanceIncompatibleShapeError(
            "different values for discriminator value path",
            Some(PropertyShapeModel.Path.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case PropertyShapeModel.Range =>
        if (
          computeStringEquality(
            superValue,
            derivedValue,
            Some(PropertyShapeModel.Range.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        ) {
          derivedValue
        } else {
          throw new InheritanceIncompatibleShapeError(
            "different values for discriminator value range",
            Some(PropertyShapeModel.Range.value.iri()),
            derivedValue.location(),
            derivedValue.position()
          )
        }

      case _ => derivedValue
    }
  }

  // this is inverted, it is safe because recursive shape does not have facets
  def computeMinRecursive(baseShape: Shape, recursiveShape: RecursiveShape): Shape = {
    restrictShape(baseShape, recursiveShape)
  }

  private def copy(shape: Shape) = {
    shape match {
      case a: AnyShape       => a.copyShape()
      case r: RecursiveShape => r.copyShape()
      case _                 => shape
    }
  }

  def computeMinShape(derivedShapeOrig: Shape, superShapeOri: Shape): Shape = {
    val superShape   = copy(superShapeOri)
    val derivedShape = derivedShapeOrig.cloneShape(Some(context.errorHandler)) // this is destructive, we need to clone
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
          } else if (
            b == DataType.Integer &&
            (s == DataType.Float ||
              s == DataType.Double ||
              s == DataType.Number)
          ) {
            computeMinScalar(baseScalar, superScalar.withDataType(DataType.Integer))
          } else if (baseScalar.dataType.option().isEmpty && superScalar.dataType.option().isDefined) {
            computeMinShape(baseScalar.withDataType(s), superScalar)
          } else {
            context.errorHandler.violation(
              InvalidTypeInheritanceErrorSpecification,
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
        case baseArray: MatrixShape if isArrayOfAnyShapes(superShape) =>
          val superArray = superShape.asInstanceOf[ArrayShape]
          computeMinMatrixWithAnyShape(baseArray, superArray)
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
        case _
            if derivedShape.meta.`type`.headOption
              .exists(_.iri() == AnyShapeModel.`type`.head.iri()) || superShape.meta.`type`.headOption
              .exists(_.iri() == AnyShapeModel.`type`.head.iri()) =>
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
            InvalidTypeInheritanceErrorSpecification,
            derivedShape,
            Some(ShapeModel.Inherits.value.iri()),
            s"Resolution error: Incompatible types [${RamlShapeTypeBeautifier
                .beautify(derivedShape.ramlSyntaxKey)}, ${RamlShapeTypeBeautifier.beautify(superShape.ramlSyntaxKey)}]"
          )
          derivedShape
      }
    } catch {
      case e: InheritanceIncompatibleShapeError =>
        if (e.isViolation) {
          context.errorHandler.violation(
            InvalidTypeInheritanceErrorSpecification,
            derivedShape.id,
            e.property.orElse(Some(ShapeModel.Inherits.value.iri())),
            e.getMessage,
            e.position.orElse(derivedShape.position()),
            e.location.orElse(derivedShape.location())
          )
        } else {
          context.errorHandler.warning(
            InvalidTypeInheritanceWarningSpecification,
            derivedShape.id,
            e.property.orElse(Some(ShapeModel.Inherits.value.iri())),
            e.getMessage,
            e.position.orElse(derivedShape.position()),
            e.location.orElse(derivedShape.location())
          )
        }
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

  protected def isGenericNodeShape(shape: Shape): Boolean = {
    shape match {
      case node: NodeShape => node.properties.isEmpty
      case _               => false
    }
  }

  protected def computeMinScalar(baseScalar: ScalarShape, superScalar: ScalarShape): ScalarShape = {
    computeNarrowRestrictions(
      ScalarShapeModel.fields,
      baseScalar,
      superScalar,
      filteredFields = Seq(ScalarShapeModel.Examples)
    )
    baseScalar
  }

  private val allShapeFields =
    (ScalarShapeModel.fields ++ ArrayShapeModel.fields ++ NodeShapeModel.fields ++ AnyShapeModel.fields).distinct

  protected def computeMinAny(baseShape: Shape, anyShape: AnyShape): Shape = {
    computeNarrowRestrictions(allShapeFields, baseShape, anyShape)
    baseShape
  }

  protected def computeMinGeneric(baseShape: NodeShape, superShape: Shape): Shape =
    restrictShape(baseShape, superShape)

  protected def computeMinMatrix(baseMatrix: MatrixShape, superMatrix: MatrixShape): Shape = {

    val superItems = superMatrix.items
    val baseItems  = baseMatrix.items
    if (Option(superItems).isDefined && Option(baseItems).isDefined) {

      val newItems = context.minShape(baseItems, superItems)
      baseMatrix.fields.setWithoutId(ArrayShapeModel.Items, newItems)

      computeNarrowRestrictions(
        ArrayShapeModel.fields,
        baseMatrix,
        superMatrix,
        filteredFields = Seq(ArrayShapeModel.Items)
      )
    } else {
      if (Option(superItems).isDefined) baseMatrix.fields.setWithoutId(ArrayShapeModel.Items, superItems)
    }

    baseMatrix
  }

  protected def isArrayOfAnyShapes(shape: Shape): Boolean =
    shape.isInstanceOf[ArrayShape] && shape.asInstanceOf[ArrayShape].items.isInstanceOf[AnyShape]

  protected def computeMinMatrixWithAnyShape(baseMatrix: MatrixShape, superArray: ArrayShape): Shape = {

    val superItems = superArray
    val baseItems  = baseMatrix.items
    if (Option(superItems).isDefined && Option(baseItems).isDefined) {

      val newItems = context.minShape(baseItems, superItems)
      baseMatrix.fields.setWithoutId(ArrayShapeModel.Items, newItems)

      computeNarrowRestrictions(
        ArrayShapeModel.fields,
        baseMatrix,
        superArray,
        filteredFields = Seq(ArrayShapeModel.Items)
      )
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
        baseTuple.fields.setWithoutId(
          TupleShapeModel.Items,
          AmfArray(superItems),
          baseTuple.fields.get(TupleShapeModel.Items).annotations
        )
        baseTuple
      } else {
        throw new InheritanceIncompatibleShapeError(
          "Cannot inherit from a tuple shape with different number of elements",
          None,
          baseTuple.location(),
          baseTuple.position()
        )
      }
    } else {
      val newItems = for {
        (baseItem, i) <- baseItems.view.zipWithIndex
      } yield {
        context.minShape(baseItem, superItems(i))
      }

      baseTuple.fields.setWithoutId(
        TupleShapeModel.Items,
        AmfArray(newItems),
        baseTuple.fields.get(TupleShapeModel.Items).annotations
      )

      computeNarrowRestrictions(
        TupleShapeModel.fields,
        baseTuple,
        superTuple,
        filteredFields = Seq(TupleShapeModel.Items)
      )

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

    computeNarrowRestrictions(
      ArrayShapeModel.fields,
      baseArray,
      superArray,
      filteredFields = Seq(ArrayShapeModel.Items)
    )

    baseArray
  }

  protected def computeMinNode(baseNode: NodeShape, superNode: NodeShape): Shape = {
    val superProperties = superNode.properties
    val baseProperties  = baseNode.properties

    type IsOverridden = Boolean
    type PropertyPath = String

    val commonProps: mutable.HashMap[PropertyPath, IsOverridden] = mutable.HashMap()

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
        val superPropOption = superProperties.find(_.path.is(path))
        val basePropOption  = baseProperties.find(_.path.is(path))
        if (keepEditingInfo) {
          superPropOption
            .map(inheritProp(superNode))
            .getOrElse {
              basePropOption.get.cloneShape(Some(context.errorHandler))
            }
//            .adopted(baseNode.id)
        } else {
          superPropOption
            .map(_.cloneShape(Some(context.errorHandler)))
            .getOrElse {
              basePropOption.get.cloneShape(Some(context.errorHandler))
            }
//            .adopted(baseNode.id)
        }
    }

    // This can be nil in the case of inheritance
    val annotations = Option(baseNode.fields.getValue(NodeShapeModel.Properties)) match {
      case Some(field) => field.annotations
      case None        => Annotations()
    }
    baseNode.fields.setWithoutId(NodeShapeModel.Properties, AmfArray(minProps.toSeq), annotations)

    computeNarrowRestrictions(
      NodeShapeModel.fields :+ DomainElementModel.CustomDomainProperties, // custom domain isn't part of nodeshape model fields
      baseNode,
      superNode,
      filteredFields = Seq(NodeShapeModel.Properties, NodeShapeModel.Examples)
    )

    // if its raml 08 i need to keep parsed json schema annotation in order to emit a valid nodeshape.
    // Remember that objects in 08 are only valid in external schemas or as formProperties under only two media types (form undercoder and formData)
    if (context.isRaml08)
      superNode.annotations.find(classOf[ParsedJSONSchema]).foreach { baseNode.annotations += _ }

    baseNode
  }

  def inheritProp(from: Shape)(prop: PropertyShape): PropertyShape = {
    val clonedProp = prop.cloneShape(Some(context.errorHandler)) // TODO this might not be working as expected
    if (clonedProp.annotations.find(classOf[InheritanceProvenance]).isEmpty) {
      clonedProp.annotations += InheritanceProvenance(from.id)
      clonedProp.id = clonedProp.id + "/inherited"
    }
    clonedProp
  }

  object UnionErrorHandler extends AMFErrorHandler {

    override def report(result: AMFValidationResult): Unit =
      throw new Exception("raising exceptions in union processing")

    def wrapContext(ctx: NormalizationContext): NormalizationContext = {
      new NormalizationContext(
        this,
        ctx.keepEditingInfo,
        ctx.profile,
        ctx.resolvedInheritanceCache
      )
    }
  }
  protected def computeMinUnion(baseUnion: UnionShape, superUnion: UnionShape): Shape = {

    val unionContext: NormalizationContext = UnionErrorHandler.wrapContext(context)
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
          throw new InheritanceIncompatibleShapeError(
            "Cannot compute inheritance for union",
            None,
            baseUnion.location(),
            baseUnion.position()
          )
        finalMinShapes
      }

    avoidDuplicatedIds(newUnionItems)
    baseUnion.fields.setWithoutId(
      UnionShapeModel.AnyOf,
      AmfArray(newUnionItems),
      baseUnion.fields.getValue(UnionShapeModel.AnyOf).annotations
    )

    computeNarrowRestrictions(
      UnionShapeModel.fields,
      baseUnion,
      superUnion,
      filteredFields = Seq(UnionShapeModel.AnyOf)
    )

    baseUnion
  }

  private def avoidDuplicatedIds(newUnionItems: Seq[Shape]): Unit =
    newUnionItems.groupBy(_.id).foreach {
      case (_, shapes) if shapes.size > 1 =>
        val counter = new IdCounter()
        shapes.foreach { shape =>
          shape.id = counter.genId(shape.id)
        }
      case _ =>
    }

  protected def computeMinUnionNode(baseUnion: UnionShape, superNode: NodeShape): Shape = {
    val unionContext: NormalizationContext = UnionErrorHandler.wrapContext(context)
    val newUnionItems = for {
      baseUnionElement <- baseUnion.anyOf
    } yield {
      unionContext.minShape(baseUnionElement, superNode)
    }

    baseUnion.fields.setWithoutId(
      UnionShapeModel.AnyOf,
      AmfArray(newUnionItems),
      baseUnion.fields.getValue(UnionShapeModel.AnyOf).annotations
    )

    computeNarrowRestrictions(UnionShapeModel.fields, baseUnion, superNode, filteredFields = Seq(UnionShapeModel.AnyOf))

    baseUnion
  }

  protected def computeMinSuperUnion(baseShape: Shape, superUnion: UnionShape): Shape = {
    val unionContext: NormalizationContext = UnionErrorHandler.wrapContext(context)
    val minItems = for {
      superUnionElement <- superUnion.anyOf
    } yield {
      try {
        val newShape = unionContext.minShape(filterBaseShape(baseShape), superUnionElement)
        newShape.withId(superUnionElement.id)
        setValuesOfUnionElement(newShape, superUnionElement)
        Some(newShape)
      } catch {
        case _: Exception => None
      }
    }
    val newUnionItems = minItems collect { case Some(s) => s }
    if (newUnionItems.isEmpty) {
      throw new InheritanceIncompatibleShapeError(
        "Cannot compute inheritance from union",
        None,
        baseShape.location(),
        baseShape.position()
      )
    }

    newUnionItems.zipWithIndex.foreach { case (shape, i) =>
      shape.id = shape.id + s"_$i"
      shape
    }

    superUnion.fields.setWithoutId(
      UnionShapeModel.AnyOf,
      AmfArray(newUnionItems),
      superUnion.fields.getValue(UnionShapeModel.AnyOf).annotations
    )

    computeNarrowRestrictions(allShapeFields, baseShape, superUnion, filteredFields = Seq(UnionShapeModel.AnyOf))
    baseShape.fields foreach { case (field, value) =>
      if (field != UnionShapeModel.AnyOf) {
        superUnion.fields.setWithoutId(field, value.value, value.annotations)
      }
    }

    superUnion.withId(baseShape.id)
  }

  private def filterBaseShape(baseShape: Shape): Shape = {
    // There are some fields of the union that we don't want to propagate to it's members
    val filteredFields =
      Seq(
        AnyShapeModel.Values,
        AnyShapeModel.DefaultValueString,
        AnyShapeModel.Default,
        AnyShapeModel.Examples,
        AnyShapeModel.Description,
        AnyShapeModel.CustomDomainProperties
      )
    val filteredBase = baseShape.copyShape()
    filteredBase.fields.filter(f => !filteredFields.contains(f._1))
    filteredBase
  }

  private def setValuesOfUnionElement(newShape: Shape, superUnionElement: Shape): Unit = {
    superUnionElement.name.option().foreach(n => newShape.withName(n))
    /*
    overrides additionalProperties value of unionElement to newShape to generate consistency with restrictShape method
    that is called when a union type is parsed as AnyShape.
     */
    (newShape, superUnionElement) match {
      case (newShape: NodeShape, superUnion: NodeShape) =>
        newShape.fields
          .getValueAsOption(NodeShapeModel.Closed)
          .map(closedValue =>
            newShape.set(
              NodeShapeModel.Closed,
              AmfScalar(superUnion.closed.value(), closedValue.value.annotations),
              closedValue.annotations
            )
          )
      case _ =>
    }
  }

  def computeMinProperty(baseProperty: PropertyShape, superProperty: PropertyShape): Shape = {
    if (isExactlyAny(baseProperty.range) && !isInferred(baseProperty) && isSubtypeOfAny(superProperty.range)) {
      context.errorHandler.violation(
        InvalidTypeInheritanceErrorSpecification,
        baseProperty,
        Some(ShapeModel.Inherits.value.iri()),
        s"Resolution error: Invalid scalar inheritance base type 'any' can't override"
      )
    } else {
      val newRange = context.minShape(baseProperty.range, superProperty.range)
      baseProperty.fields.setWithoutId(
        PropertyShapeModel.Range,
        newRange,
        baseProperty.fields.getValue(PropertyShapeModel.Range).annotations
      )

      computeNarrowRestrictions(
        PropertyShapeModel.fields,
        baseProperty,
        superProperty,
        filteredFields = Seq(PropertyShapeModel.Range)
      )
    }

    baseProperty
  }

  def computeMinFile(baseFile: FileShape, superFile: FileShape): Shape = {
    computeNarrowRestrictions(FileShapeModel.fields, baseFile, superFile)
    baseFile
  }

  private def isExactlyAny(shape: Shape)       = shape.meta == AnyShapeModel
  private def isSubtypeOfAny(shape: Shape)     = shape.meta != AnyShapeModel && shape.isInstanceOf[AnyShape]
  private def isInferred(shape: PropertyShape) = shape.range.annotations.contains(classOf[Inferred])

  val keepEditingInfo: Boolean = context.keepEditingInfo
}
