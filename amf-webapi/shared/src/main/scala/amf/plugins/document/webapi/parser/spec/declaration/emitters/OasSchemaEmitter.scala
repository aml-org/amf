package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasSchemaEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val shape = f.value.value.asInstanceOf[Shape]

    b.entry(
      "schema",
      OasTypePartEmitter(shape, ordering, references = references).emit(_)
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
