package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  ExamplesEmitter,
  RamlShapeEmitterContext,
  ShapeEmitterContext
}
import amf.plugins.domain.shapes.models.AnyShape

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
