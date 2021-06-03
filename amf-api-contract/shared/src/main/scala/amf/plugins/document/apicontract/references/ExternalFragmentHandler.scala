package amf.plugins.document.apicontract.references

import amf.core.parser.{ParsedReference, ParserContext}

trait ExternalFragmentHandler {
  def handler(reference: ParsedReference, ctx: ParserContext): ParsedReference = ???
}
