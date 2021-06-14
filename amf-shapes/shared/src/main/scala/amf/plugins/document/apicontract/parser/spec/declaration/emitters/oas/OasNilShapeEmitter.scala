package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.client.common.position.Position
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.domain.shapes.models.NilShape
import org.yaml.model.YDocument.EntryBuilder

case class OasNilShapeEmitter(nil: NilShape, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = b.entry("type", "null")

  override def position(): Position = pos(nil.annotations)
}
