package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class AvroRecordShapeEmitter(
    shape: NodeShape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends AvroComplexShapeEmitter(shape, ordering)
    with EntryEmitter {

  override def emitSpecificFields(b: EntryBuilder): Unit = {
    shape.fields.entry(NodeShapeModel.Properties).foreach { entry =>
      b.entry(
        "fields",
        _.list { partBuilder =>
          entry.array.values.foreach { case property: PropertyShape =>
            partBuilder.obj { entryBuilder =>
              AvroShapeEmitter(property, ordering).entries().foreach(_.emit(entryBuilder))
            }
          }
        }
      )
    }
  }

  override def position(): Position = pos(shape.annotations)

  def emitters(): Seq[EntryEmitter] = Seq(this)
}
