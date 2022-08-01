package amf.graphqlfederation.internal.plugins

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{
  CompilerReferenceCollector,
  ParsedDocument,
  ParserContext,
  ReferenceHandler
}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{GraphQL, Spec, Syntax}
import amf.graphql.internal.spec.document.GraphQLBaseDocumentParser
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.DEFINITION
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext

object GraphQLFederationParsePlugin extends ApiParsePlugin with GraphQLASTParserHelper {
  override def spec: Spec = GraphQL

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    GraphQLBaseDocumentParser(document)(context(document, ctx)).parseDocument()
  }

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler =
    (_: ParsedDocument, _: ParserContext) => CompilerReferenceCollector()

  private def context(document: Root, ctx: ParserContext): GraphQLFederationWebApiContext = {
    new GraphQLFederationWebApiContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      Some(WebApiDeclarations(Nil, UnhandledErrorHandler, ctx.futureDeclarations))
    )
  }

  /** media types which specifies vendors that are parsed by this plugin.
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

  private def isGraphQL(doc: AntlrParsedDocument): Boolean = collect(doc.ast.root(), Seq(DEFINITION)).nonEmpty

  override def withIdAdoption: Boolean = true // TODO pending analysis and parsing cleanup
}
