package amf.xml.internal.spec.document

import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.core.client.scala.model.document.Document
import amf.xml.internal.plugins.syntax.{XMLDocumentParserHelper, XMLParsedDocument}
import amf.xml.internal.spec.context.XMLDocContext
import amf.xml.internal.spec.domain.XMLElementParser

class XMLDocumentParser(root: XMLParsedDocument)(implicit val ctx: XMLDocContext) extends XMLDocumentParserHelper {

  val doc: Document = Document()

  def parseDocument(): Document = {
    val xmlElem = root.xml
    new XMLElementParser(xmlElem, ctx).parse(doc.withEncodes(_))
    doc
  }

}
