package amf.framework.parser

abstract class AbstractReferenceCollector {
  def traverse(document: ParsedDocument, ctx: ParserContext): Seq[Reference]
}