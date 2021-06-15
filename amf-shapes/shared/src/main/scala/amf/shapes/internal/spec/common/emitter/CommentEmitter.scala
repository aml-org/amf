package amf.shapes.internal.spec.common.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.AmfElement
import amf.core.internal.annotations.SourceAST
import amf.core.internal.render.emitters.PartEmitter
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YNode
import org.yaml.render.YamlRender

case class CommentEmitter(element: AmfElement, message: String) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    b += YNode.Empty
    b.comment(message)
    element.annotations.find(classOf[SourceAST]).map(_.ast).foreach(a => b.comment(YamlRender.render(a)))
  }

  override def position(): Position = Position.ZERO
}
