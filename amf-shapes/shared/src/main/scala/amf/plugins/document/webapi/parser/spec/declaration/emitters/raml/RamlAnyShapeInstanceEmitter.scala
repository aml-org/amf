package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{ExamplesEmitter, ShapeEmitterContext}
import amf.plugins.domain.shapes.models.AnyShape

import scala.collection.mutable.ListBuffer

object RamlAnyShapeInstanceEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: ShapeEmitterContext): RamlAnyShapeInstanceEmitter =
    new RamlAnyShapeInstanceEmitter(shape, ordering, references)(spec)
}

class RamlAnyShapeInstanceEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: ShapeEmitterContext)
    extends RamlShapeEmitter(shape, ordering, references)
    with ExamplesEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    var results = ListBuffer(super.emitters(): _*)

    emitExamples(shape, results, ordering, references)

    if (!typeEmitted) {
      val entry = MapEntryEmitter("type", "any")
      results ++= Seq(entry)
    }

    results
  }

  override val typeName: Option[String] = Some("any")
}
