package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.TupleShape
import amf.shapes.internal.domain.metamodel.ArrayShapeModel
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import amf.shapes.internal.spec.contexts.emitter.raml.RamlScalarEmitter

import scala.collection.mutable.ListBuffer

case class RamlTupleShapeEmitter(tuple: TupleShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends RamlAnyShapeEmitter(tuple, ordering, references) {
  override def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

    val fs = tuple.fields

    result += RamlTupleItemsShapeEmitter(tuple, ordering, references)

    fs.entry(ArrayShapeModel.MaxItems).map(f => result += RamlScalarEmitter("maxItems", f))
    fs.entry(ArrayShapeModel.MinItems).map(f => result += RamlScalarEmitter("minItems", f))
    fs.entry(ArrayShapeModel.UniqueItems).map(f => result += RamlScalarEmitter("uniqueItems", f))

    result
  }

  override val typeName: Option[String] = tuple.annotations.find(classOf[ExplicitField]).map(_ => "array")
}
