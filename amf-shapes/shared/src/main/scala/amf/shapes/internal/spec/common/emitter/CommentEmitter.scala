package amf.shapes.internal.spec.common.emitter

import amf.core.client.scala.model.domain.AmfElement
import amf.core.internal.annotations.SourceYPart
import amf.core.internal.render.emitters.PartEmitter
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YNode
import org.yaml.render.YamlRender

case class CommentEmitter(element: AmfElement, message: String) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    b += YNode.Empty
    b.comment(message)
    if (element != null) {
      element.annotations.find(classOf[SourceYPart]).map(_.ast).foreach(a => b.comment(YamlRender.render(a)))
    }
  }

  override def position(): Position = Position.ZERO
}
