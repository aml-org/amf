package amf.plugins.parse

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.exception.InvalidDocumentHeaderException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{EmptyFutureDeclarations, ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AsyncApi20, Vendor}
import amf.plugins.common.Async20MediaTypes
import amf.shapes.internal.spec.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.apicontract.parser.AsyncHeader
import amf.plugins.document.apicontract.parser.AsyncHeader.Async20Header
import amf.plugins.document.apicontract.parser.spec.AsyncWebApiDeclarations
import amf.plugins.document.apicontract.parser.spec.async.AsyncApi20DocumentParser

object Async20ParsePlugin extends OasLikeParsePlugin {

  override def vendor: Vendor = AsyncApi20

  override def applies(element: Root): Boolean = AsyncHeader(element).contains(Async20Header)

  override def validMediaTypesToReference: Seq[String] =
    super.validMediaTypesToReference ++ Raml10ParsePlugin.mediaTypes

  override def mediaTypes: Seq[String] = Async20MediaTypes.mediaTypes

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    implicit val newCtx: AsyncWebApiContext = context(document.location, document.references, ctx.parsingOptions, ctx)
    restrictCrossSpecReferences(document, ctx)
    val parsed = parseAsyncUnit(document)
    promoteFragments(parsed, newCtx)
  }

  private def parseAsyncUnit(root: Root)(implicit ctx: AsyncWebApiContext): BaseUnit = {
    AsyncHeader(root) match {
      case Some(Async20Header) => AsyncApi20DocumentParser(root).parseDocument()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.name)
    }
  }

  private def context(loc: String,
                      refs: Seq[ParsedReference],
                      options: ParsingOptions,
                      wrapped: ParserContext,
                      ds: Option[AsyncWebApiDeclarations] = None): Async20WebApiContext = {
    // ensure unresolved references in external fragments are not resolved with main api definitions
    val cleanContext = wrapped.copy(futureDeclarations = EmptyFutureDeclarations())
    cleanContext.globalSpace = wrapped.globalSpace
    new Async20WebApiContext(loc, refs, cleanContext, ds, options = options)
  }
}
