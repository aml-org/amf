package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  OasLikeShapeEmitterContext,
  ShapeEmitterContext
}
import amf.plugins.domain.shapes.models.UnionShape
import org.yaml.model.YDocument.EntryBuilder

case class OasAnyOfShapeEmitter(shape: UnionShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      spec.anyOfKey,
      _.list { b =>
        val emitters = shape.anyOf.zipWithIndex map {
          case (s: Shape, i: Int) =>
            OasTypePartEmitter(s,
                               ordering,
                               ignored = Nil,
                               references,
                               pointer = pointer ++ Seq("anyOf", s"$i"),
                               schemaPath)
        }
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(shape.annotations)
}
