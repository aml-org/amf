package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.domain.ShapeModel.Not
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.emitters.PartEmitter
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YNode

import scala.language.postfixOps

case class BooleanSchemaEmitter(shape: Shape) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    b += {
      if (shape.fields.exists(Not)) YNode(false) else YNode(true)
    }
  }
  override def position(): Position = {
    pos(shape.annotations)
  }
}
