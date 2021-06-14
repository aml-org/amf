package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.RamlShapeEmitterContext
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument.EntryBuilder

case class RamlNotConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends EntryEmitter {

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
