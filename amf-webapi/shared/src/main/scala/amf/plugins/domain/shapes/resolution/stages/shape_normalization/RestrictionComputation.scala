package amf.plugins.domain.shapes.resolution.stages.shape_normalization

import amf.core.metamodel.Field
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.{AmfArray, AmfElement, AmfScalar, Shape}
import amf.core.parser.Annotations
import amf.plugins.domain.shapes.annotations.InheritanceProvenance
import amf.plugins.domain.shapes.metamodel._

trait RestrictionComputation {

  val keepEditingInfo: Boolean

  protected def computeNarrowRestrictions(fields: Seq[Field],
                                          baseShape: Shape,
                                          superShape: Shape,
                                          filteredFields: Seq[Field] = Seq.empty): Shape = {
    fields.foreach { f =>
      if (!filteredFields.contains(f)) {
        val baseValue  = Option(baseShape.fields.getValue(f))
        val superValue = Option(superShape.fields.getValue(f))
        baseValue match {
          case Some(bvalue) if superValue.isEmpty => baseShape.set(f, bvalue.value, bvalue.annotations)

          case None if superValue.isDefined       =>
            val finalAnnotations = Annotations(superValue.get.annotations)
            if (keepEditingInfo) inheritAnnotations(finalAnnotations, superShape)
            baseShape.set(f, superValue.get.value, finalAnnotations)

          case Some(bvalue) if superValue.isDefined =>
            val finalValue = computeNarrow(f, bvalue.value, superValue.get.value)
            val finalAnnotations = Annotations(bvalue.annotations)
            if (finalValue != bvalue.value && keepEditingInfo) inheritAnnotations(finalAnnotations, superShape)
            baseShape.set(f, finalValue, finalAnnotations)
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
    restriction.fields.foreach {
      case (field, baseValue) =>
        if (field != NodeShapeModel.Inherits) {
          Option(shape.fields.getValue(field)) match {
            case Some(superValue) => shape.set(field, computeNarrow(field, baseValue.value, superValue.value))
            case None             => shape.fields.setWithoutId(field, baseValue.value, baseValue.annotations)
          }
        }
    }
    shape
  }

  protected def computeNumericRestriction(comparison: String, lvalue: AmfElement, rvalue: AmfElement): AmfElement = {
    lvalue match {
      case scalar: AmfScalar
          if Option(scalar.value).isDefined && rvalue.isInstanceOf[AmfScalar] && Option(
            rvalue.asInstanceOf[AmfScalar].value).isDefined =>
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
          case _ => throw new Exception(s"Unknown numeric comparison $comparison")
        }
      case _ =>
        throw new InheritanceIncompatibleShapeError("Cannot compare non numeric or missing values")
    }
  }

  protected def computeStringEquality(lvalue: AmfElement, rvalue: AmfElement): Boolean = {
    lvalue match {
      case scalar: AmfScalar
          if Option(scalar.value).isDefined && rvalue.isInstanceOf[AmfScalar] && Option(
            rvalue.asInstanceOf[AmfScalar].value).isDefined =>
        val lstr = scalar.toString
        val rstr = rvalue.asInstanceOf[AmfScalar].toString
        lstr == rstr
      case _ =>
        throw new InheritanceIncompatibleShapeError("Cannot compare non numeric or missing values")
    }
  }

  protected def stringValue(value: AmfElement): Option[String] = {
    value match {
      case scalar: AmfScalar
        if Option(scalar.value).isDefined && value.isInstanceOf[AmfScalar] && Option(
          value.asInstanceOf[AmfScalar].value).isDefined =>
        Some(scalar.toString)
      case _ => None
    }
  }

  protected def computeNumericComparison(comparison: String, lvalue: AmfElement, rvalue: AmfElement): Boolean = {
    lvalue match {
      case scalar: AmfScalar
          if Option(scalar.value).isDefined && rvalue.isInstanceOf[AmfScalar] && Option(
            rvalue.asInstanceOf[AmfScalar].value).isDefined =>
        val lnum = scalar.toNumber
        val rnum = rvalue.asInstanceOf[AmfScalar].toNumber

        comparison match {
          case "<=" =>
            lnum.intValue() <= rnum.intValue()
          case ">=" =>
            lnum.intValue() >= rnum.intValue()
          case _ => throw new InheritanceIncompatibleShapeError(s"Unknown numeric comparison $comparison")
        }
      case _ =>
        throw new InheritanceIncompatibleShapeError("Cannot compare non numeric or missing values")
    }
  }

  protected def computeBooleanComparison(lcomparison: Boolean,
                                         rcomparison: Boolean,
                                         lvalue: AmfElement,
                                         rvalue: AmfElement): Boolean = {
    lvalue match {
      case scalar: AmfScalar
          if Option(scalar.value).isDefined && rvalue.isInstanceOf[AmfScalar] && Option(
            rvalue.asInstanceOf[AmfScalar].value).isDefined =>
        val lbool = scalar.toBool
        val rbool = rvalue.asInstanceOf[AmfScalar].toBool
        lbool == lcomparison && rbool == rcomparison
      case _ =>
        throw new InheritanceIncompatibleShapeError("Cannot compare non boolean or missing values")
    }
  }

  protected def computeNarrow(field: Field, baseValue: AmfElement, superValue: AmfElement): AmfElement = {
    field match {

      case ShapeModel.Name => {
        val baseStrValue = stringValue(baseValue)
        val superStrValue = stringValue(superValue)
        if (superStrValue.isDefined && (baseStrValue.isEmpty || baseStrValue.get == "schema")) {
          superValue
        } else {
          baseValue
        }
      }

      case NodeShapeModel.MinProperties =>
        if (computeNumericComparison("<=", superValue, baseValue)) {
          computeNumericRestriction("max", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for min-properties than base type for minProperties")
        }

      case NodeShapeModel.MaxProperties =>
        if (computeNumericComparison(">=", superValue, baseValue)) {
          computeNumericRestriction("min", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for max-properties than base type for maxProperties")
        }

      case ScalarShapeModel.MinLength =>
        if (computeNumericComparison("<=", superValue, baseValue)) {
          computeNumericRestriction("max", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for min-length than base type for maxProperties")
        }

      case ScalarShapeModel.MaxLength =>
        if (computeNumericComparison(">=", superValue, baseValue)) {
          computeNumericRestriction("min", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for max-length than base type for maxProperties")
        }

      case ScalarShapeModel.Minimum =>
        if (computeNumericComparison("<=", superValue, baseValue)) {
          computeNumericRestriction("max", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for min-minimum than base type for minimum")
        }

      case ScalarShapeModel.Maximum =>
        if (computeNumericComparison(">=", superValue, baseValue)) {
          computeNumericRestriction("min", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for maximum than base type for maximum")
        }

      case ArrayShapeModel.MinItems =>
        if (computeNumericComparison("<=", superValue, baseValue)) {
          computeNumericRestriction("max", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for minItems than base type for minItems")
        }

      case ArrayShapeModel.MaxItems =>
        if (computeNumericComparison(">=", superValue, baseValue)) {
          computeNumericRestriction("min", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for maxItems than base type for maxItems")
        }

      case ScalarShapeModel.Format =>
        if (computeStringEquality(superValue, baseValue)) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("different values for format constraint")
        }

      case ScalarShapeModel.Pattern =>
        if (computeStringEquality(superValue, baseValue)) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("different values for pattern constraint")
        }

      case NodeShapeModel.Discriminator =>
        if (computeStringEquality(superValue, baseValue)) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("different values for discriminator constraint")
        }

      case NodeShapeModel.DiscriminatorValue =>
        if (computeStringEquality(superValue, baseValue)) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("different values for discriminator value constraint")
        }

      case ShapeModel.Values =>
        val baseEnumeration  = baseValue.asInstanceOf[AmfArray].values.map(_.toString)
        val superEnumeration = superValue.asInstanceOf[AmfArray].values.map(_.toString)
        if (superEnumeration.forall(e => baseEnumeration.contains(e))) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("Values in super type not found in the subtype enumeration")
        }

      case ArrayShapeModel.UniqueItems =>
        if (computeBooleanComparison(lcomparison = true, rcomparison = true, superValue, baseValue) ||
            computeBooleanComparison(lcomparison = false, rcomparison = false, superValue, baseValue) ||
            computeBooleanComparison(lcomparison = false, rcomparison = true, superValue, baseValue)) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("different values for unique items constraint")
        }

      case PropertyShapeModel.MinCount =>
        if (computeNumericComparison("<=", superValue, baseValue)) {
          computeNumericRestriction("max", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for minCount than base type for minCount")
        }

      case PropertyShapeModel.MaxCount =>
        if (computeNumericComparison(">=", superValue, baseValue)) {
          computeNumericRestriction("min", superValue, baseValue)
        } else {
          throw new InheritanceIncompatibleShapeError(
            "Resolution error: sub type has a weaker constraint for maxCount than base type for maxCount")
        }

      case PropertyShapeModel.Path =>
        if (computeStringEquality(superValue, baseValue)) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("different values for discriminator value path")
        }

      case PropertyShapeModel.Range =>
        if (computeStringEquality(superValue, baseValue)) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("different values for discriminator value range")
        }

      case NodeShapeModel.Closed =>
        if (computeBooleanComparison(lcomparison = true, rcomparison = true, superValue, baseValue) ||
            computeBooleanComparison(lcomparison = false, rcomparison = false, superValue, baseValue) ||
            computeBooleanComparison(lcomparison = true, rcomparison = false, superValue, baseValue)) {
          baseValue
        } else {
          throw new InheritanceIncompatibleShapeError("closed shapes cannot inherit from open shapes")
        }

      case _ => baseValue
    }
  }
}
