package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.domain.shapes.models.NilShape

case class RamlNilShapeEmitter(shape: NilShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
