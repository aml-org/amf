package amf.apiinstance.internal.spec.context

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.domain.Declarations

case class FlexGWConfigContext(rootContextDocument: String, refs: Seq[ParsedReference], parsingOptions: ParsingOptions, ctx: ParserContext, some: Some[Declarations]) {
  def eh: AMFErrorHandler = ctx.eh

  var path: Option[String] = None
  def withPath(somePath: Option[String]): Unit = {
    path = somePath
  }

}

