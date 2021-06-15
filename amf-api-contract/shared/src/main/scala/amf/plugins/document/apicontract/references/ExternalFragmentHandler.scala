package amf.plugins.document.apicontract.references

import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}

trait ExternalFragmentHandler {
  def handler(reference: ParsedReference, ctx: ParserContext): ParsedReference = ???
}
