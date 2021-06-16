package amf.shapes.internal.spec.oas.emitter

import amf.core.client.common.position.Position
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.NilShape
import org.yaml.model.YDocument.EntryBuilder

case class OasNilShapeEmitter(nil: NilShape, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = b.entry("type", "null")

  override def position(): Position = pos(nil.annotations)
}
