package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.domain.metamodel._
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class AvroFixedShapeEmitter(
    shape: ScalarShape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends AvroComplexShapeEmitter(shape, ordering)
    with EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry("type", "fixed")
    emitSpecificFields(b)
    emitCommonFields(b)
  }

  override def emitSpecificFields(b: EntryBuilder): Unit = {
    shape.fields.entry(AnyShapeModel.Size).foreach(f => b.entry("size", f.scalar.toString.toInt))
  }

  override def position(): Position = pos(shape.annotations)

  def emitters(): Seq[EntryEmitter] = Seq(this)
}
