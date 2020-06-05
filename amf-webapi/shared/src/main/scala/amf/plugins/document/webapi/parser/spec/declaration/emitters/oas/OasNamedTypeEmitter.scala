package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasNamedTypeEmitter(shape: Shape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil,
                               customName: Option[String] = None)(implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = customName.getOrElse(shape.name.option().getOrElse("schema")) // this used to throw an exception, but with the resolution optimizacion, we use the father shape, so it could have not name (if it's from an endpoint for example, and you want to write a new single shape, like a json schema)
    b.entry(name, OasTypePartEmitter(shape, ordering, references = references, pointer = pointer :+ name).emit(_))
  }

  override def position(): Position = pos(shape.annotations)
}
