package amf.shapes.internal.plugins.parser

import amf.core.client.scala.exception.UnsupportedParsedDocumentException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParserContext, SyamlParsedDocument}
import amf.core.internal.parser.Root
import amf.core.internal.plugins.parse.AMFGraphParsePlugin
import amf.shapes.internal.plugins.document.graph.parser.FlattenedJsonLdInstanceUnitGraphParser

object AMFJsonLDSchemaGraphParsePlugin extends AMFGraphParsePlugin {

  override def applies(element: Root): Boolean = element.parsed match {
    case parsed: SyamlParsedDocument => FlattenedJsonLdInstanceUnitGraphParser.canParse(parsed, aliases)
    case _                           => false
  }
  override def parse(document: Root, ctx: ParserContext): BaseUnit = document.parsed match {
    case parsed: SyamlParsedDocument =>
      FlattenedJsonLdInstanceUnitGraphParser(ctx.config, aliases)
        .parse(parsed.document, effectiveUnitUrl(document.location, ctx.parsingOptions))
    case _ => throw UnsupportedParsedDocumentException
  }

  override def mediaTypes: Seq[String] = Seq(
    "application/schemald+json"
  )
}
