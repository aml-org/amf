package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasAndConstraintEmitter(shape: Shape,
                                   ordering: SpecOrdering,
                                   references: Seq[BaseUnit],
                                   pointer: Seq[String] = Nil,
                                   schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[OasTypePartEmitter] = shape.and.zipWithIndex map {
    case (s: Shape, i: Int) =>
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
