package amf.shapes.internal.spec.raml.parser.expression

import amf.shapes.client.scala.model.domain.UnresolvedShape
import amf.shapes.internal.spec.common.parser.ShapeParserContext
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
    shape.unresolved(shape.name.value(), Nil, Some(part.getOrElse(YNode.Null).location))(context)
  }
}
