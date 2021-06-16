package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.BaseEmitters.MapEntryEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.NilShape
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext

case class RamlNilShapeEmitter(shape: NilShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends RamlAnyShapeEmitter(shape, ordering, references) {

  override def emitters(): Seq[EntryEmitter] = {
    var result: Seq[EntryEmitter] = super.emitters()
    if (!typeEmitted) {
      val entry = MapEntryEmitter("type", "nil")
      result = result ++ Seq(entry)
    }
    result
  }

  override val typeName: Option[String] = Some("nil")
}
