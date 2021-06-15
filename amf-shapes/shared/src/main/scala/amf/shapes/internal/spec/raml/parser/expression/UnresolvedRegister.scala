package amf.shapes.internal.spec.raml.parser.expression

import amf.shapes.client.scala.domain.models.UnresolvedShape
import amf.shapes.internal.spec.ShapeParserContext
import org.yaml.model.{YNode, YPart}

private[expression] sealed trait UnresolvedRegister {
  def register(unresolved: UnresolvedShape)
}

private[expression] case class EmptyRegister() extends UnresolvedRegister {
  override def register(unresolved: UnresolvedShape): Unit = Unit
}

private[expression] case class ContextRegister(context: ShapeParserContext, part: Option[YPart])
    extends UnresolvedRegister {
  override def register(shape: UnresolvedShape): Unit = {
    shape.withContext(context)
    shape.unresolved(shape.name.value(), part.getOrElse(YNode.Null))(context)
  }
}
