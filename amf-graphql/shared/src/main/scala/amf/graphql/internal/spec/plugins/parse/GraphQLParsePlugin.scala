package amf.graphql.internal.spec.plugins.parse

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
import amf.core.internal.parser.domain.DotQualifiedNameExtractor
import amf.core.internal.remote.{GraphQL, Spec, Syntax}
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.document.GraphQLBaseDocumentParser

object GraphQLParsePlugin extends GraphQLBasedParsePlugin {
  override def spec: Spec = GraphQL

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    GraphQLBaseDocumentParser(document)(context(document, ctx)).parseDocument()
  }

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler =
    (_: ParsedDocument, _: ParserContext) => CompilerReferenceCollector()

  private def context(document: Root, ctx: ParserContext): GraphQLWebApiContext = {
    new GraphQLWebApiContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      Some(WebApiDeclarations(Nil, UnhandledErrorHandler, ctx.futureDeclarations, DotQualifiedNameExtractor))
    )
  }

  /** media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Syntax.graphQLMimes.toSeq

  override def withIdAdoption: Boolean = true // TODO pending analysis and parsing cleanup
}
