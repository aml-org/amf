package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import org.yaml.model.YDocument.EntryBuilder

case class OasAndConstraintEmitter(
    shape: Shape,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    pointer: Seq[String] = Nil,
    schemaPath: Seq[(String, String)] = Nil
)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[OasTypePartEmitter] = shape.and.zipWithIndex map { case (s: Shape, i: Int) =>
    OasTypePartEmitter(s, ordering, ignored = Nil, references, pointer = pointer ++ Seq("allOf", s"$i"), schemaPath)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "allOf",
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}
