package amf.antlr.internal.plugins.syntax

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.parse.AMFSyntaxParsePlugin
import amf.core.client.scala.parse.document.{ParsedDocument, ParserContext}
import amf.core.internal.remote.Mimes.`application/graphql`
import amf.core.internal.remote.Syntax
import org.mulesoft.antlrast.platform.PlatformGraphQLFederationParser

object GraphQLFederationSyntaxParsePlugin extends AMFSyntaxParsePlugin {

  override def parse(text: CharSequence, mediaType: String, ctx: ParserContext): ParsedDocument = {
    val input  = text.toString
    val parser = new PlatformGraphQLFederationParser()
    val ast    = parser.parse(ctx.rootContextDocument, input)
    AntlrParsedDocument(ast, None)
  }

  override def mediaTypes: Seq[String] = Syntax.graphQLMimes.toSeq

  override val id: String = "graphql-federation-parse"

  override def applies(element: CharSequence): Boolean = true

  override def priority: PluginPriority = HighPriority

  override def mainMediaType: String = `application/graphql`
}
