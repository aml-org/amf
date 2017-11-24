package amf.plugins.document.graph.references

import amf.compiler.ParsedDocument
import amf.framework.parser.{AbstractReferenceCollector, ParserContext}

class AMFGraphReferenceCollector extends AbstractReferenceCollector{
  override def traverse(document: ParsedDocument, ctx: ParserContext) = Nil
}
