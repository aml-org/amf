package amf.xml.internal.plugins.syntax

import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.parse.AMFSyntaxParsePlugin
import amf.core.client.scala.parse.document.{ParsedDocument, ParserContext}
import amf.core.internal.remote.Mimes

object XMLSyntaxParsePlugin extends AMFSyntaxParsePlugin {

  override def parse(text: CharSequence, mediaType: String, ctx: ParserContext): ParsedDocument = {
    val xml = XMLWithSourceMapsLoader.loadString(text.toString)
    new XMLParsedDocument(xml)
  }

  override def mediaTypes: Seq[String] = Seq(
    Mimes.`application/xml`,
    "text/xml"
  )

  override def mainMediaType: String = "text/xml"

  override val id: String = "xml-parse"

  override def applies(element: CharSequence): Boolean = true

  override def priority: PluginPriority = HighPriority
}
