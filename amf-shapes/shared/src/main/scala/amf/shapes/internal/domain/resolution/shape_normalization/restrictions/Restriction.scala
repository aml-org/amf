package amf.shapes.internal.domain.resolution.shape_normalization.restrictions

import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfScalar, ScalarNode}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, NodeShapeModel, ScalarShapeModel}
import amf.shapes.internal.domain.resolution.shape_normalization.AmfElementComparer.{
  areEqualBooleans,
  areEqualStrings,
  areExpectedBooleans,
  computeNumericRestriction,
  incompatibleException,
  lessOrEqualThan,
  moreOrEqualThan
}
import amf.shapes.internal.domain.resolution.shape_normalization.InheritanceIncompatibleShapeError

trait Restriction {

  def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement
}

object NameRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    val derivedStrValue = maybeAsString(derivedValue)
    val superStrValue   = maybeAsString(superValue)
    (superStrValue, derivedStrValue) match {
      case (Some(_), None | Some("schema")) => superValue
      case _                                => derivedValue
    }
  }

  private def maybeAsString(value: AmfElement): Option[String] = {
    value match {
      case scalar: AmfScalar => Option(scalar.value).map(_.toString)
      case _                 => None
    }
  }
}

object RangeRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      areEqualStrings(
        superValue,
        derivedValue,
        incompatibleException(PropertyShapeModel.Range, derivedValue)
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
  }
}

object PathRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      areEqualStrings(
        superValue,
        derivedValue,
        incompatibleException(PropertyShapeModel.Path, derivedValue)
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
  }
}

object MinPropertiesRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      lessOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(NodeShapeModel.MinProperties, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "max",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for min-properties than base type for minProperties",
        Some(NodeShapeModel.MinProperties.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}

object MaxPropertiesRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      moreOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(NodeShapeModel.MaxProperties, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "min",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for max-properties than base type for maxProperties",
        Some(NodeShapeModel.MaxProperties.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}

object MinLengthRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      lessOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(ScalarShapeModel.MinLength, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "max",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for min-length than base type for maxProperties",
        Some(ScalarShapeModel.MinLength.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}

object MinimumRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      lessOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(ScalarShapeModel.Minimum, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "max",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for min-minimum than base type for minimum",
        Some(ScalarShapeModel.Minimum.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}

object MaxLengthRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      moreOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(ScalarShapeModel.MaxLength, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "min",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for max-length than base type for maxProperties",
        Some(ScalarShapeModel.MaxLength.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}

object MaximumRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      moreOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(ScalarShapeModel.Maximum, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "min",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for maximum than base type for maximum",
        Some(ScalarShapeModel.Maximum.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}

object MinItemsRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      lessOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(ArrayShapeModel.MinItems, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "max",
        superValue,
        derivedValue
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
  }
}

object MaxItemsRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      moreOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(ArrayShapeModel.MaxItems, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "min",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for maxItems than base type for maxItems",
        Some(ArrayShapeModel.MaxItems.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}

object FormatRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      areEqualStrings(
        superValue,
        derivedValue,
        incompatibleException(ScalarShapeModel.Format, derivedValue)
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
  }
}

object PatternRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      areEqualStrings(
        superValue,
        derivedValue,
        incompatibleException(ScalarShapeModel.Pattern, derivedValue)
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
  }
}

object DiscriminatorRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      !areEqualStrings(
        superValue,
        derivedValue,
        incompatibleException(NodeShapeModel.Discriminator, derivedValue)
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
  }
}

object DiscriminatorValueRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      !areEqualStrings(
        superValue,
        derivedValue,
        incompatibleException(NodeShapeModel.DiscriminatorValue, derivedValue)
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
  }
}

object ValuesRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    computeEnum(
      derivedValue.asInstanceOf[AmfArray].values,
      superValue.asInstanceOf[AmfArray].values
    )
    derivedValue
  }

  protected def computeEnum(
      derivedEnumeration: Seq[AmfElement],
      superEnumeration: Seq[AmfElement]
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
          val ds           = derivedEnumeration.asInstanceOf[Seq[ScalarNode]]
          ds.foreach { e =>
            if (!superScalars.contains(e.value.value())) {
              throw new InheritanceIncompatibleShapeError(
                s"Values in subtype enumeration (${ds.map(_.value).mkString(",")}) not found in the supertype enumeration (${superScalars
                    .mkString(",")})",
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
}

object UniqueItemsRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      areEqualBooleans(
        superValue,
        derivedValue,
        incompatibleException(ArrayShapeModel.UniqueItems, derivedValue)
      ) ||
      areEqualBooleans(
        superValue,
        derivedValue,
        incompatibleException(ArrayShapeModel.UniqueItems, derivedValue)
      ) ||
      areExpectedBooleans(
        superValue,
        derivedValue,
        expectedLeft = false,
        expectedRight = true,
        incompatibleException(ArrayShapeModel.UniqueItems, derivedValue)
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
  }
}

object MinCountRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      lessOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(PropertyShapeModel.MinCount, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "max",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for minCount than base type for minCount",
        Some(PropertyShapeModel.MinCount.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}

object MaxCountRestriction extends Restriction {
  override def restrict(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    if (
      moreOrEqualThan(
        superValue,
        derivedValue,
        incompatibleException(PropertyShapeModel.MaxCount, derivedValue)
      )
    ) {
      computeNumericRestriction(
        "min",
        superValue,
        derivedValue
      )
    } else {
      throw new InheritanceIncompatibleShapeError(
        "Resolution error: sub type has a weaker constraint for maxCount than base type for maxCount",
        Some(PropertyShapeModel.MaxCount.value.iri()),
        derivedValue.location(),
        derivedValue.position()
      )
    }
  }
}
