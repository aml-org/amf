package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.{ArrayShape, ScalarShape}
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class AvroArrayShapeEmitter(
    arrayShape: ArrayShape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends AvroComplexShapeEmitter(arrayShape, ordering) {
  override def emitSpecificFields(b: EntryBuilder): Unit = {
    arrayShape.items match {
      case scalar: ScalarShape =>
        val avroType = spec.getAvroType(scalar).getOrElse("string")
        b.entry("items", avroType)
      case _ =>
        b.entry(
          "items",
          _.obj { entryBuilder =>
            val itemsEmitter = AvroShapeEmitter(arrayShape.items, ordering)
            val entries      = itemsEmitter.entries()
            traverse(entries, entryBuilder)
          }
        )
    }
  }

  override def emitCommonFields(b: EntryBuilder): Unit = {}

  override def position(): Position = pos(arrayShape.annotations)

  def emitters(): Seq[EntryEmitter] = Seq(this)

}
