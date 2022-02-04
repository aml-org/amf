package amf.apiinstance.internal.spec.context

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}

case class EnvoyConfigContext(rootContextDocument: String, refs: Seq[ParsedReference], parsingOptions: ParsingOptions, ctx: ParserContext, some: Some[Nothing]) {
  def eh: AMFErrorHandler = ctx.eh
}
