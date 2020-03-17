package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{pos, raw}
import amf.core.emitter.PartEmitter
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.domain.shapes.models.ShapeHelpers
import org.yaml.model.YDocument.PartBuilder

case class RamlExternalSourceEmitter(shape: Shape with ShapeHelpers, references: Seq[BaseUnit]) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    references
      .collectFirst({
        case ex: ExternalFragment if ex.encodes.id.equals(shape.externalSourceID.getOrElse("")) => ex.encodes
      })
      .flatMap(ex => ex.raw.option())
      .foreach { raw(b, _) }
  }

  override def position(): Position = pos(shape.annotations)
}
