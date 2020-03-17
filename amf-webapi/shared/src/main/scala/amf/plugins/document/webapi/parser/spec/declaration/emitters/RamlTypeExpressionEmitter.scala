package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{pos, raw}
import amf.core.emitter.PartEmitter
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.domain.shapes.models.ShapeHelpers
import org.yaml.model.YDocument.PartBuilder

case class RamlTypeExpressionEmitter(shape: Shape with ShapeHelpers)(implicit spec: SpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = raw(b, shape.typeExpression(spec.eh))

  override def position(): Position = pos(shape.annotations)
}
