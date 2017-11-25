package amf.core.parser

abstract class AbstractReferenceCollector {
  def traverse(document: ParsedDocument, ctx: ParserContext): Seq[Reference]
}