package amf.apicontract.internal.spec.avro.emitters.domain

import amf.apicontract.internal.spec.avro.emitters.context.AvroShapeEmitterContext
import amf.core.client.scala.model.domain.ScalarNode
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class AvroEnumShapeEmitter(
    shape: ScalarShape,
    ordering: SpecOrdering
)(implicit spec: AvroShapeEmitterContext)
    extends AvroComplexShapeEmitter(shape, ordering)
    with EntryEmitter {

  override def emitSpecificFields(b: EntryBuilder): Unit = {
    shape.fields.entry(AnyShapeModel.Values).foreach { entry =>
      b.entry(
        "symbols",
        _.list { lb =>
          entry.array.values.foreach { case scalarNode: ScalarNode =>
            lb += scalarNode.value.value()
          }
        }
      )
    }
  }

  override def position(): Position = pos(shape.annotations)

  def emitters(): Seq[EntryEmitter] = Seq(this)
}
