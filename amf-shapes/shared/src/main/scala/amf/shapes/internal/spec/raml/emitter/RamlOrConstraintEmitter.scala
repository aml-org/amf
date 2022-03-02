package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder
import amf.core.internal.utils._
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO

case class RamlOrConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends EntryEmitter {

  val emitters: Seq[Raml10TypePartEmitter] = shape.or.map { s =>
    Raml10TypePartEmitter(s, ordering, None, Nil, references)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "or".asRamlAnnotation,
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}
