package amf.plugins.document.webapi.references

import amf.core.parser.{AbstractReferenceCollector, ParsedDocument, ParserContext}

class PayloadReferenceCollector extends AbstractReferenceCollector {
  override def traverse(document: ParsedDocument, ctx: ParserContext) = Nil
}
