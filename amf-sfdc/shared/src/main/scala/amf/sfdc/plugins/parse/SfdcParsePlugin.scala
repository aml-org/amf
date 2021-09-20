package amf.sfdc.plugins.parse

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{CompilerReferenceCollector, ParsedDocument, ParserContext, ReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Syntax.Json
import amf.core.internal.remote.{GraphQL, Hint, Spec, Syntax}
import amf.sfdc.internal.spec.context.GraphQLWebApiContext
import amf.sfdc.internal.spec.document.GraphQLDocumentParser
import amf.sfdc.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.sfdc.internal.spec.parser.syntax.TokenTypes.{DEFINITION, DOCUMENT, TYPE_SYSTEM_DEFINITION}
import amf.sfdc.internal.spec.context.GraphQLWebApiContext
import org.mulesoft.antlrast.ast.Node

private[amf] case object Sfdc extends Spec {
  override val id: String        = "Sfdc"
  override val mediaType: String = "application/json"
}

object SfdcHint extends Hint(Sfdc, Json)

object SfdcParsePlugin extends ApiParsePlugin with GraphQLASTParserHelper {
  override def spec: Spec = Sfdc

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val foobar = GraphQLDocumentParser(document)(context(document, ctx)).parseDocument()
    println("foobar")
    foobar
  }

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler =
    (_: ParsedDocument, _: ParserContext) =>
      CompilerReferenceCollector()

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
  override def mediaTypes: Seq[String] = Set("application/json").toSeq

  override def applies(element: Root): Boolean = {
    true
    /*
    element.parsed match {
      case antlrDoc: AntlrParsedDocument =>
        isGraphQL(antlrDoc)
      case _ =>
        false
    }

     */
  }

  private def isGraphQL(doc: AntlrParsedDocument): Boolean = {
    path(doc.ast.root(), Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION)) match {
      case Some(_: Node) => true
      case _             => false
    }
  }
}
