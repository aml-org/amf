package amf.plugins.document.vocabularies.spec

import amf.core.parser.{Declarations, ParserContext}

class DialectContext(private val wrapped: ParserContext, private val internalDec: Option[Declarations] = None) extends ParserContext(wrapped.refs) {

  val declarations: Declarations = internalDec.getOrElse(Declarations(Seq(), errorHandler = Some(this)))

}
