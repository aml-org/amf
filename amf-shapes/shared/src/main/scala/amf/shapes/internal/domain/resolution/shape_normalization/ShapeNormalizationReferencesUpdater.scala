package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain._
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, FieldEntry}
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, NodeShapeModel}

case class ShapeNormalizationReferencesUpdater(context: NormalizationContext) {
  private var alreadyUpdated: Set[AmfObject] = Set.empty
  def update(e: AmfElement): AmfElement = {
    e match {
      case s: Shape => updateShape(s)
      case e        => e
    }
  }

  def updateShape(shape: Shape): Shape = {
    if (shape.isLink) {
      // TODO: this is horrible but this leaves it like before
      RecursiveShape(shape).withFixPoint(shape.id)
    } else {
      val updated = AnyShapeAdjuster(retrieveLatestVersionOf(shape))
      // TODO: this is horrible
      UnnecessaryAnnotationsRemover(updated)
      updated
    }
  }

  private def retrieveLatestVersionOf(shape: Shape) = {
    context.resolvedInheritanceIndex.get(shape.id) match {
      case Some(resolvedInheritance) => resolvedInheritance
      case _                         => shape
    }
  }

  def updateFields(o: AmfObject): Unit = {
    ifNotUpdated(o) { o =>
      val fieldEntries = o.fields.fields()
      fieldEntries.foreach { case FieldEntry(field, value) =>
        value.value match {
          case s: Shape    => updateShapeField(o, field, s)
          case a: AmfArray => updateArrayField(o, field, a)
          case _           => // ignore
        }
      }
    }
  }

  private def ifNotUpdated(o: AmfObject)(fn: AmfObject => Unit): Unit = {
    if (!alreadyUpdated.contains(o)) {
      fn(o)
      alreadyUpdated = alreadyUpdated + o
    }
  }

  private def updateArrayField(o: AmfObject, field: Field, a: AmfArray): Unit = {
    val updatedValues = a.values.map {
      case s: Shape => updateShape(s)
      case v        => v // ignore
    }
    setArrayField(o, field, updatedValues)
  }

  private def updateShapeField(o: AmfObject, field: Field, s: Shape): Unit = {
    val updatedValue = updateShape(s)
    setField(o, field, updatedValue)
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
