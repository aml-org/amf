package amf.plugins.document.webapi.parser.spec.jsonschema.emitter

import amf.core.annotations.{ExplicitField, SynthesizedField}
import amf.core.emitter.BaseEmitters.{ValueEmitter, pos}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, FieldEntry, Position}
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasEntryShapeEmitter
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, NodeShapeModel}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument

case class UnevaluatedInfo(key: String, booleanField: Field, schemaField: Field)

object UnevaluatedEmitter {
  val unevaluatedPropertiesInfo: UnevaluatedInfo = UnevaluatedInfo("unevaluatedProperties",
                                                                   NodeShapeModel.UnevaluatedProperties,
                                                                   NodeShapeModel.UnevaluatedPropertiesSchema)
  val unevaluatedItemsInfo: UnevaluatedInfo =
    UnevaluatedInfo("unevaluatedItems", ArrayShapeModel.UnevaluatedItems, ArrayShapeModel.UnevaluatedItemsSchema)
}

class UnevaluatedEmitter(private val shape: AnyShape,
                         private val emissionInfo: UnevaluatedInfo,
                         ordering: SpecOrdering,
                         references: Seq[BaseUnit],
                         pointer: Seq[String] = Nil,
                         schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  private val fs                                              = shape.fields
  private val UnevaluatedInfo(key, booleanField, schemaField) = emissionInfo

  override def emit(b: YDocument.EntryBuilder): Unit = {
    fs.entry(booleanField).filter(f => isExplicit(f) && !isSynthesized(f)) match {
      case Some(f) => ValueEmitter(key, f).emit(b)
      case _ =>
        fs.entry(schemaField).foreach { f =>
          OasEntryShapeEmitter(key, f.element.asInstanceOf[Shape], ordering, references, pointer, schemaPath).emit(b)
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
