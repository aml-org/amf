package amf.shapes.internal.spec.raml.parser.expression

import amf.core.internal.parser.domain.SearchScope
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.ShapeParserContext

private[expression] sealed trait DeclarationFinder {
  def find(name: String): Option[AnyShape]
}

private[expression] case class DummyDeclarationFinder() extends DeclarationFinder {
  override def find(name: String): Option[AnyShape] = None
}

private[expression] case class ContextDeclarationFinder(context: ShapeParserContext) extends DeclarationFinder {
  override def find(name: String): Option[AnyShape] = context.findType(name, SearchScope.Named)
}
