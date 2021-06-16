package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext

import scala.collection.mutable.ListBuffer

object RamlAnyShapeEmitter {
  def apply(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: RamlShapeEmitterContext): RamlAnyShapeEmitter =
    new RamlAnyShapeEmitter(shape, ordering, references)
}

class RamlAnyShapeEmitter(shape: AnyShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends RamlShapeEmitter(shape, ordering, references)
    with ExamplesEmitter {
  override def emitters(): Seq[EntryEmitter] = {
    val results = ListBuffer(super.emitters(): _*)

    emitExamples(shape, results, ordering, references)

    results
  }

  override val typeName: Option[String] = Some("any")
}
