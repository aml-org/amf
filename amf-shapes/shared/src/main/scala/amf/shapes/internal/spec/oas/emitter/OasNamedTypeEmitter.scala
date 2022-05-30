package amf.shapes.internal.spec.oas.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasNamedTypeEmitter(
    shape: Shape,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    pointer: Seq[String] = Nil,
    customName: Option[String] = None
)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = computeName
    b.entry(name, OasTypePartEmitter(shape, ordering, references = references, pointer = pointer :+ name).emit(_))
  }

  private def computeName = customName.orElse(shape.name.option()).getOrElse("schema")

  override def position(): Position = pos(shape.annotations)
}
