package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils._
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import org.yaml.model.YDocument.EntryBuilder

case class RamlXoneConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends EntryEmitter {

  val emitters: Seq[Raml10TypePartEmitter] = shape.xone.map { s =>
    Raml10TypePartEmitter(s, ordering, None, Nil, references)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "xone".asRamlAnnotation,
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}
