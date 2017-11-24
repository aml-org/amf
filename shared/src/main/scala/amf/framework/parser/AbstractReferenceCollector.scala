package amf.framework.parser

import amf.compiler.ParsedDocument
import amf.core.{AMFCompiler => ReferenceCompiler}
import amf.validation.Validation

abstract class AbstractReferenceCollector {
  def traverse(document: ParsedDocument, validation: Validation, ctx: ParserContext): Seq[Reference]
}
