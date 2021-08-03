package amf.graphql.plugins.parse

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.antlr.internal.plugins.syntax.AntlrSyntaxParsePlugin
import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{CompilerReferenceCollector, ParsedDocument, ParserContext, ReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{GraphQL, Syntax, Vendor}
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.document.GraphQLDocumentParser
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{DEFINITION, DOCUMENT, TYPE_SYSTEM_DEFINITION}
import org.mulesoft.antlrast.ast.Node

object GraphQLParsePlugin extends ApiParsePlugin with GraphQLASTParserHelper {
  override protected def vendor: Vendor = GraphQL

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    GraphQLDocumentParser(document)(context(document, ctx)).parseDocument()
  }

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = (_: ParsedDocument, _: ParserContext) => CompilerReferenceCollector()

  private def context(document: Root, ctx: ParserContext): GraphQLWebApiContext = {
    new GraphQLWebApiContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      Some(WebApiDeclarations(Nil, UnhandledErrorHandler, ctx.futureDeclarations))
    )
  }

  /**
    * media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Syntax.graphQLMimes.toSeq

  override def applies(element: Root): Boolean = {
    element.parsed match {
      case antlrDoc: AntlrParsedDocument =>
        isGraphQL(antlrDoc)
      case _ =>
        false
    }
  }

  private def isGraphQL(doc: AntlrParsedDocument): Boolean = {
    path(doc.ast.root(), Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION)) match {
      case Some(_: Node) => true
      case _ => false
    }
  }
}
