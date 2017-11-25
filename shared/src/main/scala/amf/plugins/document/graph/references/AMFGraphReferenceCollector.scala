package amf.plugins.document.graph.references

import amf.core.parser.{AbstractReferenceCollector, ParsedDocument, ParserContext}

class AMFGraphReferenceCollector extends AbstractReferenceCollector{
  override def traverse(document: ParsedDocument, ctx: ParserContext) = Nil
}
