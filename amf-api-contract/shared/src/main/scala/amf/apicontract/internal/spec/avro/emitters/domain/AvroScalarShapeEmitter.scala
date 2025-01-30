package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.annotations.AVROSchemaType
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class AvroScalarShapeEmitter(
    shape: ScalarShape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val avroType = shape.annotations.find(classOf[AVROSchemaType])
    avroType.foreach(avroSchemaType => b.entry("type", avroSchemaType.avroType))
    shape.fields.entry(ShapeModel.Name).foreach(f => b.entry("name", f.scalar.toString))
    shape.fields.entry(ShapeModel.Description).foreach(f => b.entry("doc", f.scalar.toString))
    shape.fields.entry(ShapeModel.Default).foreach { _ =>
      b.entry("default", DataNodeEmitter(shape.default, ordering)(spec.eh).emit(_))
    }
  }

  override def position(): Position = pos(shape.annotations)

  def emitters(): Seq[EntryEmitter] = Seq(this)
}
