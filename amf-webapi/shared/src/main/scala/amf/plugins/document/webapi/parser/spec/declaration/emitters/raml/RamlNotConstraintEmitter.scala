package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument.EntryBuilder
import amf.core.utils.AmfStrings

case class RamlNotConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
