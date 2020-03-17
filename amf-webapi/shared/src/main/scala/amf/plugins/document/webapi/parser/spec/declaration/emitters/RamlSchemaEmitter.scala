package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument.EntryBuilder

case class RamlSchemaEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    f.value.value match {
      case shape: AnyShape =>
        b.entry(
          "type",
          emitters.Raml10TypePartEmitter(shape, ordering, None, references = references).emit(_)
        )
      case _ => // ignore?
    }
  }

  override def position(): Position = pos(f.value.annotations)
}
