package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.RamlShapeEmitterContext
import amf.plugins.domain.shapes.metamodel.TupleShapeModel
import amf.plugins.domain.shapes.models.TupleShape
import org.yaml.model.YDocument.EntryBuilder

case class RamlTupleItemsShapeEmitter(tuple: TupleShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "tuple".asRamlAnnotation,
      _.list(
        b => tuple.items.foreach(RamlTupleItemEmitter(_, ordering, references).emit(b))
      )
    )
  }

  override def position(): Position = pos(tuple.fields.getValue(TupleShapeModel.TupleItems).annotations)
}
