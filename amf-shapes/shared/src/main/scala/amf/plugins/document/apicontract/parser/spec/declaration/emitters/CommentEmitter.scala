package amf.plugins.document.apicontract.parser.spec.declaration.emitters

import amf.core.annotations.SourceAST
import amf.core.emitter.PartEmitter
import amf.core.model.domain.AmfElement
import amf.core.parser.Position
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
