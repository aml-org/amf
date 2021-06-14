package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder
import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.internal.parser.domain.{FieldEntry, Fields}
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.IriTemplateEmitter
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import org.yaml.model.YDocument.EntryBuilder

case class OasOrConstraintEmitter(shape: Shape,
                                  ordering: SpecOrdering,
                                  references: Seq[BaseUnit],
                                  pointer: Seq[String] = Nil,
                                  schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[OasTypePartEmitter] = shape.or.zipWithIndex map {
    case (s: Shape, i: Int) =>
      OasTypePartEmitter(s, ordering, ignored = Nil, references, pointer = pointer ++ Seq("anyOf", s"$i"), schemaPath)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "anyOf",
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}
