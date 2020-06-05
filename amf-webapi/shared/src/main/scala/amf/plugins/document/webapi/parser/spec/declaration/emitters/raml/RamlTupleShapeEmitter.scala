package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.annotations.ExplicitField
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.emitter.raml.{RamlScalarEmitter, RamlSpecEmitterContext}
import amf.plugins.domain.shapes.metamodel.ArrayShapeModel
import amf.plugins.domain.shapes.models.TupleShape

import scala.collection.mutable.ListBuffer

case class RamlTupleShapeEmitter(tuple: TupleShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
