package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.MapEntryEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext

import scala.collection.mutable.ListBuffer

object RamlAnyShapeInstanceEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
      spec: RamlShapeEmitterContext
  ): RamlAnyShapeInstanceEmitter =
    new RamlAnyShapeInstanceEmitter(shape, ordering, references)(spec)
}

class RamlAnyShapeInstanceEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends RamlShapeEmitter(shape, ordering, references)
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
