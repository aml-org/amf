package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration
import org.yaml.model.YDocument.EntryBuilder
import amf.core.utils.AmfStrings

case class RamlAndConstraintEmitter(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {

  val emitters: Seq[Raml10TypePartEmitter] = shape.and.map { s =>
    declaration.emitters.Raml10TypePartEmitter(s, ordering, None, Nil, references)
  }

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "and".asRamlAnnotation,
      _.list { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      }
    )
  }

  override def position(): Position = emitters.map(_.position()).sortBy(_.line).headOption.getOrElse(ZERO)
}
