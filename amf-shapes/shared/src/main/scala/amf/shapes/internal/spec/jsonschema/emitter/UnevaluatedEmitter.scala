package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.{ExplicitField, SynthesizedField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, FieldEntry}
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, NodeShapeModel}
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import amf.shapes.internal.spec.oas.emitter.OasEntryShapeEmitter
import org.yaml.model.YDocument

case class UnevaluatedInfo(key: String, booleanField: Field, schemaField: Field)

object UnevaluatedEmitter {
  val unevaluatedPropertiesInfo: UnevaluatedInfo = UnevaluatedInfo(
    "unevaluatedProperties",
    NodeShapeModel.UnevaluatedProperties,
    NodeShapeModel.UnevaluatedPropertiesSchema
  )
  val unevaluatedItemsInfo: UnevaluatedInfo =
    UnevaluatedInfo("unevaluatedItems", ArrayShapeModel.UnevaluatedItems, ArrayShapeModel.UnevaluatedItemsSchema)
}

class UnevaluatedEmitter(
    private val shape: AnyShape,
    private val emissionInfo: UnevaluatedInfo,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    pointer: Seq[String] = Nil,
    schemaPath: Seq[(String, String)] = Nil
)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {

  private val fs                                              = shape.fields
  private val UnevaluatedInfo(key, booleanField, schemaField) = emissionInfo

  override def emit(b: YDocument.EntryBuilder): Unit = {
    fs.entry(booleanField).filter(f => isExplicit(f) && !isSynthesized(f)) match {
      case Some(f) => ValueEmitter(key, f).emit(b)
      case _ =>
        fs.entry(schemaField).foreach { f =>
          OasEntryShapeEmitter(key, f.element.asInstanceOf[Shape], ordering, references, pointer, schemaPath)
            .emit(b)
        }
    }
  }

  override def position(): Position = pos(
    getAnnotationsFrom(NodeShapeModel.UnevaluatedProperties)
      .orElse(getAnnotationsFrom(NodeShapeModel.UnevaluatedPropertiesSchema))
      .getOrElse(Annotations())
  )

  private def getAnnotationsFrom(field: Field) = fs.entry(field).map(_.value.annotations)
  private def isExplicit(f: FieldEntry)        = f.value.annotations.contains(classOf[ExplicitField])
  private def isSynthesized(f: FieldEntry)     = f.value.annotations.contains(classOf[SynthesizedField])
}
