package amf.sfdc.plugins.parse

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
import amf.core.internal.remote.Syntax.Json
import amf.core.internal.remote.{Hint, Spec}
import amf.sfdc.internal.spec.context.SfdcWebApiContext
import amf.sfdc.internal.spec.document.SfdcDocumentParser

private[amf] case object Sfdc extends Spec {
  override val id: String        = "Sfdc"
  override val mediaType: String = "application/json"
}

object SfdcHint extends Hint(Sfdc, Json)

object SfdcParsePlugin extends ApiParsePlugin {
  override def spec: Spec = Sfdc

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val foobar = SfdcDocumentParser(document)(context(document, ctx)).parseDocument()
    foobar
  }

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler =
    (_: ParsedDocument, _: ParserContext) => CompilerReferenceCollector()

  private def context(document: Root, ctx: ParserContext): SfdcWebApiContext = {
    new SfdcWebApiContext(
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

  override def applies(element: Root): Boolean = true
}
