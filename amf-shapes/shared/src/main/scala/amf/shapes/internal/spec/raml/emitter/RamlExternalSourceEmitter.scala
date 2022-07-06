package amf.shapes.internal.spec.raml.emitter

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.{pos, raw}
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.client.scala.model.domain.ShapeHelpers
import org.yaml.model.YDocument.PartBuilder

case class RamlExternalSourceEmitter(shape: Shape with ShapeHelpers, references: Seq[BaseUnit]) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    references
      .collectFirst({
        case ex: ExternalFragment if ex.encodes.id.equals(shape.externalSourceID.getOrElse("")) => ex.encodes
      })
      .flatMap(ex => ex.raw.option())
      .foreach {
        raw(b, _)
      }
  }

  override def position(): Position = pos(shape.annotations)
}
