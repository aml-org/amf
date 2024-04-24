package amf.xml.internal.plugins.parse

import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.ParserContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Mimes, Spec}
import amf.xml.internal.plugins.syntax.XMLParsedDocument
import amf.xml.internal.spec.context.XMLDocContext
import amf.xml.internal.spec.document.XMLDocumentParser

private case object XML extends Spec {

  override val id: String = "XML"
  override val mediaType: String = Mimes.`application/xml`
}

object XMLParsePlugin extends ApiParsePlugin {

  override def spec: Spec = XML

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    document.parsed match {
      case doc: XMLParsedDocument =>
        new XMLDocumentParser(doc)(new XMLDocContext(ctx)).parseDocument()
      case _                      =>
        throw new Exception("Unsupported parsed document, expecting XML document")
    }
  }

  override def mediaTypes: Seq[String] = Seq(
    Mimes.`application/xml`
  )

  override def applies(element: Root): Boolean = true
}
