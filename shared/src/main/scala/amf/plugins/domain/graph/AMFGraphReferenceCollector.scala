package amf.plugins.domain.graph

import amf.compiler.{AbstractReferenceCollector, ParsedDocument}
import amf.spec.ParserContext
import amf.validation.Validation

class AMFGraphReferenceCollector extends AbstractReferenceCollector{
  override def traverse(document: ParsedDocument, validation: Validation, ctx: ParserContext) = Nil
}
