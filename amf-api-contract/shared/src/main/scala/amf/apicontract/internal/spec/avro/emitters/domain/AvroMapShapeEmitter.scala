package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class AvroMapShapeEmitter(
    mapShape: NodeShape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends AvroComplexShapeEmitter(mapShape, ordering) {
  override def emitSpecificFields(b: EntryBuilder): Unit = {
    mapShape.additionalPropertiesSchema match {
      case scalar: ScalarShape =>
        val avroType = spec.getAvroType(scalar).getOrElse("string")
        b.entry("values", avroType)
      case _ =>
        b.entry(
          "values",
          _.obj { entryBuilder =>
            val itemsEmitter =
              AvroShapeEmitter(mapShape.additionalPropertiesSchema, ordering)
            val entries = itemsEmitter.entries()
            traverse(entries, entryBuilder)
          }
        )
    }
  }
  override def emitCommonFields(b: EntryBuilder): Unit = {}

  override def position(): Position = pos(mapShape.annotations)

  def emitters(): Seq[EntryEmitter] = Seq(this)

}
