package amf.antlr.internal.plugins.syntax

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.core.client.scala.parse.AMFSyntaxParsePlugin
import amf.core.client.scala.parse.document.{ParsedDocument, ParserContext}
import org.mulesoft.antlrast.ast.{AST, Parser}

trait BaseAntlrSyntaxParsePlugin extends AMFSyntaxParsePlugin {

  def parser(): Parser
  override def parse(text: CharSequence, mediaType: String, ctx: ParserContext): ParsedDocument = {
    val input = text.toString
    if (input.trim.isEmpty) AntlrParsedDocument(AST(), None)
    else {
      val ast = parser().parse(ctx.rootContextDocument, input)
      AntlrParsedDocument(ast, None)
    }
  }
}
