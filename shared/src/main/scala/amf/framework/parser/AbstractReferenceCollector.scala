package amf.framework.parser

import amf.compiler.ParsedDocument
import amf.core.{AMFCompiler => ReferenceCompiler}

abstract class AbstractReferenceCollector {
  def traverse(document: ParsedDocument, ctx: ParserContext): Seq[Reference]
}
