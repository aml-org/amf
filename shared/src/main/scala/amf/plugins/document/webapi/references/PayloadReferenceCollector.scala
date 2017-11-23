package amf.plugins.document.webapi.references

import amf.compiler.ParsedDocument
import amf.framework.parser.AbstractReferenceCollector
import amf.spec.ParserContext
import amf.validation.Validation

class PayloadReferenceCollector extends AbstractReferenceCollector {
  override def traverse(document: ParsedDocument, validation: Validation, ctx: ParserContext) = Nil
}
