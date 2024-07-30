package amf.apicontract.internal.spec.async

import amf.apicontract.internal.spec.async.AsyncHeader._
import amf.apicontract.internal.spec.async.parser.context.{Async2WebApiContext, AsyncWebApiContext}
import amf.apicontract.internal.spec.async.parser.document
import amf.apicontract.internal.spec.avro.AvroParsePlugin
import amf.apicontract.internal.spec.common.AsyncWebApiDeclarations
import amf.apicontract.internal.spec.oas.OasLikeParsePlugin
import amf.apicontract.internal.spec.raml.Raml10ParsePlugin
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.exception.InvalidDocumentHeaderException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{EmptyFutureDeclarations, ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AsyncApi20, Mimes, Spec}

object Async20ParsePlugin extends OasLikeParsePlugin {

  override def spec: Spec = AsyncApi20

  override def applies(element: Root): Boolean = AsyncHeader(element).isDefined

  override def validSpecsToReference: Seq[Spec] =
    super.validSpecsToReference ++ Seq(Raml10ParsePlugin.spec, AvroParsePlugin.spec)

  override def mediaTypes: Seq[String] = Seq(Mimes.`application/yaml`, Mimes.`application/json`)

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    val header = parseHeader(document)
    implicit val newCtx: AsyncWebApiContext =
      context(document.location, document.references, ctx.parsingOptions, ctx, spec = header.spec)
    restrictCrossSpecReferences(document, ctx)
    val parsed = parseAsyncUnit(header, document)
    promoteFragments(parsed, newCtx)
  }

  private def parseHeader(root: Root): AsyncHeader =
    AsyncHeader(root).getOrElse(throw new InvalidDocumentHeaderException(spec.id))

  private def parseAsyncUnit(header: AsyncHeader, root: Root)(implicit ctx: AsyncWebApiContext): BaseUnit = {
    header match {
      case Async20Header => document.AsyncApi20DocumentParser(root).parseDocument()
      case Async21Header => document.AsyncApi21DocumentParser(root).parseDocument()
      case Async22Header => document.AsyncApi22DocumentParser(root).parseDocument()
      case Async23Header => document.AsyncApi23DocumentParser(root).parseDocument()
      case Async24Header => document.AsyncApi24DocumentParser(root).parseDocument()
      case Async25Header => document.AsyncApi25DocumentParser(root).parseDocument()
      case Async26Header => document.AsyncApi26DocumentParser(root).parseDocument()
      case _ =>
        throw new InvalidDocumentHeaderException(spec.id)
    }
  }

  private def context(
      loc: String,
      refs: Seq[ParsedReference],
      options: ParsingOptions,
      wrapped: ParserContext,
      ds: Option[AsyncWebApiDeclarations] = None,
      spec: Spec
  ): Async2WebApiContext = {
    // ensure unresolved references in external fragments are not resolved with main api definitions
    val cleanContext = wrapped.copy(futureDeclarations = EmptyFutureDeclarations())
    cleanContext.globalSpace = wrapped.globalSpace
    Async2WebApiContext(loc, refs, cleanContext, ds, options, spec)
  }
}
