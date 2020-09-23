package amf.plugins.document.webapi.parser.spec.raml.expression

import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.domain.shapes.models.UnresolvedShape
import org.yaml.model.{YNode, YPart}

private[expression] sealed trait UnresolvedRegister {
  def register(unresolved: UnresolvedShape)
}

private[expression] case class EmptyRegister() extends UnresolvedRegister {
  override def register(unresolved: UnresolvedShape): Unit = Unit
}

private[expression] case class ContextRegister(context: WebApiContext, part: Option[YPart])
    extends UnresolvedRegister {
  override def register(shape: UnresolvedShape): Unit = {
    shape.withContext(context)
    shape.unresolved(shape.name.value(), part.getOrElse(YNode.Null))(context)
  }
}
