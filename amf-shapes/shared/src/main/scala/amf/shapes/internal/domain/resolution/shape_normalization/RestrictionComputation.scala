package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain._
import amf.core.internal.annotations.InheritanceProvenance
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.metamodel.{Field, Obj}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.domain.metamodel._
import amf.shapes.internal.domain.resolution.shape_normalization.restrictions._

private[shape_normalization] trait RestrictionComputation {

  val keepEditingInfo: Boolean

  protected def computeNarrowRestrictions(meta: Obj, base: Shape, superShape: Shape, ignore: Seq[Field]): Shape = {
    computeNarrowRestrictions(meta.fields, base, superShape, ignore)
  }

  protected def computeNarrowRestrictions(
      fields: Seq[Field],
      baseShape: Shape,
      superShape: Shape,
      filteredFields: Seq[Field] = Seq.empty
  ): Shape = {
    fields.foreach { f =>
      if (!filteredFields.contains(f)) {
        val baseValue  = baseShape.fields.getValueAsOption(f)
        val superValue = superShape.fields.getValueAsOption(f)
        (baseValue, superValue) match {
          case (Some(base), None) => baseShape.set(f, base.value, base.annotations)

          case (None, Some(superVal)) =>
            val finalAnnotations = Annotations(superVal.annotations)
            if (keepEditingInfo) inheritAnnotations(finalAnnotations, superShape)
            baseShape.fields.setWithoutId(f, superVal.value, finalAnnotations)

          case (Some(bvalue), Some(superVal)) =>
            val finalAnnotations = Annotations(bvalue.annotations)
            val finalValue       = computeNarrow(f, bvalue.value, superVal.value)
            if (finalValue != bvalue.value && keepEditingInfo) inheritAnnotations(finalAnnotations, superShape)
            val effective = finalValue.add(finalAnnotations)
            baseShape.fields.setWithoutId(f, effective, finalAnnotations)
          case _ => // ignore
        }
      }
    }

    baseShape
  }

  private def inheritAnnotations(annotations: Annotations, from: Shape) = {
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
          case None             => shape.fields.setWithoutId(field, derivedValue.value, derivedValue.annotations)
        }
      }
    }
    shape
  }

  protected def computeNarrow(field: Field, derivedValue: AmfElement, superValue: AmfElement): AmfElement = {
    field match {
      case ShapeModel.Name                   => NameRestriction.restrict(field, derivedValue, superValue)
      case NodeShapeModel.MinProperties      => MinPropertiesRestriction.restrict(field, derivedValue, superValue)
      case NodeShapeModel.MaxProperties      => MaxPropertiesRestriction.restrict(field, derivedValue, superValue)
      case ScalarShapeModel.MinLength        => MinLengthRestriction.restrict(field, derivedValue, superValue)
      case ScalarShapeModel.MaxLength        => MaxLengthRestriction.restrict(field, derivedValue, superValue)
      case ScalarShapeModel.Minimum          => MinimumRestriction.restrict(field, derivedValue, superValue)
      case ScalarShapeModel.Maximum          => MaximumRestriction.restrict(field, derivedValue, superValue)
      case ArrayShapeModel.MinItems          => MinItemsRestriction.restrict(field, derivedValue, superValue)
      case ArrayShapeModel.MaxItems          => MaxItemsRestriction.restrict(field, derivedValue, superValue)
      case ScalarShapeModel.Format           => FormatRestriction.restrict(field, derivedValue, superValue)
      case ScalarShapeModel.Pattern          => PatternRestriction.restrict(field, derivedValue, superValue)
      case NodeShapeModel.Discriminator      => DiscriminatorRestriction.restrict(field, derivedValue, superValue)
      case NodeShapeModel.DiscriminatorValue => DiscriminatorValueRestriction.restrict(field, derivedValue, superValue)
      case ShapeModel.Values                 => ValuesRestriction.restrict(field, derivedValue, superValue)
      case ArrayShapeModel.UniqueItems       => UniqueItemsRestriction.restrict(field, derivedValue, superValue)
      case PropertyShapeModel.MinCount       => MinCountRestriction.restrict(field, derivedValue, superValue)
      case PropertyShapeModel.MaxCount       => MaxCountRestriction.restrict(field, derivedValue, superValue)
      case PropertyShapeModel.Path           => PathRestriction.restrict(field, derivedValue, superValue)
      case PropertyShapeModel.Range          => RangeRestriction.restrict(field, derivedValue, superValue)
      case _                                 => derivedValue
    }
  }
}
