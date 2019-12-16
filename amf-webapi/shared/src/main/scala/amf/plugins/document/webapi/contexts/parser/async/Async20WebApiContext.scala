package amf.plugins.document.webapi.contexts.parser.async

import amf.core.parser.{ErrorHandler, ParsedReference, ParserContext}
import amf.core.remote.{AsyncApi20, Vendor}
import amf.plugins.document.webapi.parser.spec.async.Async20Syntax
import amf.plugins.document.webapi.parser.spec.{AsyncWebApiDeclarations, SpecSyntax}

class Async20WebApiContext(loc: String,
                           refs: Seq[ParsedReference],
                           private val wrapped: ParserContext,
                           private val ds: Option[AsyncWebApiDeclarations] = None,
                           parserCount: Option[Int] = None,
                           override val eh: Option[ErrorHandler] = None)
    extends AsyncWebApiContext(loc, refs, wrapped, ds, parserCount, eh) {
  override val factory: Async20VersionFactory = Async20VersionFactory(this)
  override val vendor: Vendor                 = AsyncApi20
  override val syntax: SpecSyntax             = Async20Syntax
}
