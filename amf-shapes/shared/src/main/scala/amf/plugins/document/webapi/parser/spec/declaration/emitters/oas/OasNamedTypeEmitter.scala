package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasNamedTypeEmitter(shape: Shape,
                               ordering: SpecOrdering,
                               references: Seq[BaseUnit],
                               pointer: Seq[String] = Nil,
                               customName: Option[String] = None)(implicit spec: ShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = computeName
    b.entry(name, OasTypePartEmitter(shape, ordering, references = references, pointer = pointer :+ name).emit(_))
  }

  private def computeName = customName.orElse(shape.name.option()).getOrElse("schema")

  override def position(): Position = pos(shape.annotations)
}
