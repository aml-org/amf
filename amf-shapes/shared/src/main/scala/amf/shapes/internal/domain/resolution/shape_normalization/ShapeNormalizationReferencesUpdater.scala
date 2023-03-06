package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain._
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, FieldEntry}
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, NodeShapeModel}

case class ShapeNormalizationReferencesUpdater(context: NormalizationContext) {
  private var visited: Set[String] = Set.empty

  def update(shape: Shape): Shape = {
    val updated = AnyShapeAdjuster(retrieveLatestVersionOf(shape))

    // TODO: this is horrible
    UnnecessaryAnnotationsRemover(updated)

    if (!visited.contains(shape.id)) {
      visited = visited + shape.id
      updateFields(updated)
    }

    updated
  }

  private def retrieveLatestVersionOf(shape: Shape) = {
    context.resolvedInheritanceCache.get(shape.id) match {
      case Some(resolvedInheritance) => resolvedInheritance
      case _                         => shape
    }
  }

  private def updateFields(shape: Shape): Unit = {
    val fieldEntries = shape.fields.fields()
    fieldEntries.foreach { case FieldEntry(field, value) =>
      value.value match {
        case s: Shape                          => updateShapeField(shape, field, s)
        case a: AmfArray if isArrayOfShapes(a) => updateShapeArrayField(shape, field, a)
        case _                                 => // ignore
      }
    }
  }

  private def isArrayOfShapes(a: AmfArray): Boolean = a.values.forall(v => v.isInstanceOf[Shape])

  private def updateShapeArrayField(shape: Shape, field: Field, a: AmfArray): Unit = {
    val updatedValues = a.values.map {
      case s: Shape => update(s)
      case v        => v // ignore
    }
    setArrayField(shape, field, updatedValues)
  }

  private def updateShapeField(shape: Shape, field: Field, s: Shape): Unit = {
    val updatedValue = update(s)
    setField(shape, field, updatedValue)
  }

  private def setField(obj: AmfObject, field: Field, value: AmfElement): Unit = {
    val annotations: Annotations = annotationsFromField(obj, field)
    obj.setWithoutId(field, value, annotations)
  }

  private def setArrayField(obj: AmfObject, field: Field, values: Seq[AmfElement]): Unit = {
    val annotations: Annotations = annotationsFromField(obj, field)
    obj.setArrayWithoutId(field, values, annotations)
  }

  private def annotationsFromField(obj: AmfObject, field: Field) = {
    if (shouldKeepAnnotations(field)) {
      val annotations = obj.fields
        .getValueAsOption(field)
        .map(_.annotations)
        .getOrElse(Annotations())
      annotations
    } else {
      Annotations()
    }
  }

  // TODO: this is horrible
  private def shouldKeepAnnotations(field: Field): Boolean = {
    field match {
      case ArrayShapeModel.Items                     => false
      case NodeShapeModel.Properties                 => false
      case NodeShapeModel.AdditionalPropertiesSchema => false
      case _                                         => true
    }
  }
}
