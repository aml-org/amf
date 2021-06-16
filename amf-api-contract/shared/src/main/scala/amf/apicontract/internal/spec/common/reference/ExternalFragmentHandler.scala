package amf.apicontract.internal.spec.common.reference

import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}

trait ExternalFragmentHandler {
  def handler(reference: ParsedReference, ctx: ParserContext): ParsedReference = ???
}
