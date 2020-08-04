package amf.plugins.document.webapi.parser.spec.common.emitters

import amf.core.emitter.{EntryEmitter, PartEmitter}
import amf.core.parser.Position
import org.yaml.model.YDocument

case class EntryToPartEmitterAdapter(e: EntryEmitter) extends PartEmitter {
  override def emit(b: YDocument.PartBuilder): Unit = b.obj(e.emit)
  override def position(): Position                 = e.position()
}
