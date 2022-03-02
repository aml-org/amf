package amf.shapes.internal.spec.raml.emitter

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder
import amf.core.internal.utils._
import amf.shapes.client.scala.model.domain.AnyShape

case class RamlNotConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends EntryEmitter {

  val emitter: Raml10TypePartEmitter =
    Raml10TypePartEmitter(shape.not.asInstanceOf[AnyShape], ordering, None, Nil, references)

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "not".asRamlAnnotation,
      p => emitter.emit(p)
    )
  }

  override def position(): Position = emitter.position()
}
