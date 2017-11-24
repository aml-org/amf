package amf.plugins.document.webapi.references

import amf.compiler.ParsedDocument
import amf.framework.parser.{AbstractReferenceCollector, ParserContext}

class PayloadReferenceCollector extends AbstractReferenceCollector {
  override def traverse(document: ParsedDocument, ctx: ParserContext) = Nil
}
