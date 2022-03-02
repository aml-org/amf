package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils._
import amf.shapes.client.scala.model.domain.TupleShape
import amf.shapes.internal.domain.metamodel.TupleShapeModel
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class RamlTupleItemsShapeEmitter(tuple: TupleShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "tuple".asRamlAnnotation,
      _.list(b => tuple.items.foreach(RamlTupleItemEmitter(_, ordering, references).emit(b)))
    )
  }

  override def position(): Position = pos(tuple.fields.getValue(TupleShapeModel.TupleItems).annotations)
}
