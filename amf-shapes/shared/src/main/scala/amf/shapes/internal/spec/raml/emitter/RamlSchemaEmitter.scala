package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class RamlSchemaEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    f.value.value match {
      case shape: AnyShape =>
        b.entry(
          "type",
          Raml10TypePartEmitter(shape, ordering, None, references = references).emit(_)
        )
      case _ => // ignore?
    }
  }

  override def position(): Position = pos(f.value.annotations)
}
