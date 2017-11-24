package amf.plugins.document.graph.references

import amf.compiler.ParsedDocument
import amf.framework.parser.{AbstractReferenceCollector, ParserContext}
import amf.validation.Validation

class AMFGraphReferenceCollector extends AbstractReferenceCollector{
  override def traverse(document: ParsedDocument, validation: Validation, ctx: ParserContext) = Nil
}
