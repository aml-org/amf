package amf.xml.internal.plugins.syntax

import org.xml.sax.Locator
import scala.xml._
import parsing.NoBindingFactoryAdapter


trait WithLocation extends NoBindingFactoryAdapter {
  var locator: org.xml.sax.Locator = _

  // Get location
  abstract override def setDocumentLocator(locator: Locator) {
    this.locator = locator
    super.setDocumentLocator(locator)
  }

  abstract override def createNode(pre: String, label: String, attrs: MetaData, scope: NamespaceBinding, children: List[Node]): Elem = (
    super.createNode(pre, label, attrs, scope, children)
      % Attribute("line", Text(locator.getLineNumber.toString), Null)
      % Attribute("column", Text(locator.getColumnNumber.toString), Null)
    )
}

object XMLWithSourceMapsLoader extends factory.XMLLoader[Elem] {
  // Keeping ConsoleErrorHandler for good measure
  override def adapter = new parsing.NoBindingFactoryAdapter with parsing.ConsoleErrorHandler with WithLocation
}