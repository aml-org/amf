package amf.apicontract.internal.spec.common.emitter

import amf.core.client.common.position.Position
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import org.yaml.model.YDocument

case class EntryToPartEmitterAdapter(e: EntryEmitter) extends PartEmitter {
  override def emit(b: YDocument.PartBuilder): Unit = b.obj(e.emit)
  override def position(): Position                 = e.position()
}
