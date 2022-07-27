package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.UnionShape
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class OasAnyOfShapeEmitter(
    shape: UnionShape,
    ordering: SpecOrdering,
    references: Seq[BaseUnit],
    pointer: Seq[String] = Nil,
    schemaPath: Seq[(String, String)] = Nil
)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      spec.anyOfKey,
      _.list { b =>
        val emitters = shape.anyOf.zipWithIndex map { case (s: Shape, i: Int) =>
          OasTypePartEmitter(
            s,
            ordering,
            ignored = Nil,
            references,
            pointer = pointer ++ Seq("anyOf", s"$i"),
            schemaPath
          )
        }
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}
