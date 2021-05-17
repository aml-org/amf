package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{RamlShapeEmitterContext, ShapeEmitterContext}
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
