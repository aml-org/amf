package amf.plugins.document.webapi.parser.spec.raml.expression

import amf.core.parser.SearchScope
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.domain.shapes.models.AnyShape

private[expression] sealed trait DeclarationFinder {
  def find(name: String): Option[AnyShape]
}

private[expression] case class DummyDeclarationFinder() extends DeclarationFinder {
  override def find(name: String): Option[AnyShape] = None
}

private[expression] case class ContextDeclarationFinder(context: WebApiContext) extends DeclarationFinder {
  override def find(name: String): Option[AnyShape] = context.declarations.findType(name, SearchScope.Named)
}