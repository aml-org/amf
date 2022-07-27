package amf.apicontract.internal.spec.common.emitter

import org.mulesoft.common.client.lexical.Position
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import org.yaml.model.YDocument

case class EntryToPartEmitterAdapter(e: EntryEmitter) extends PartEmitter {
  override def emit(b: YDocument.PartBuilder): Unit = b.obj(e.emit)
  override def position(): Position                 = e.position()
}
