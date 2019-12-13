package amf.plugins.document.webapi.contexts.parser.oas
import amf.plugins.document.webapi.parser.spec.{OasWebApiDeclarations, SpecSyntax}
import amf.core.remote.{Oas30, Vendor}
import amf.core.parser.{ParserContext, ErrorHandler, ParsedReference}
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax

class Oas3WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        private val wrapped: ParserContext,
                        private val ds: Option[OasWebApiDeclarations] = None,
                        parserCount: Option[Int] = None,
                        override val eh: Option[ErrorHandler] = None)
  extends OasWebApiContext(loc, refs, wrapped, ds, parserCount, eh) {
  override val factory: Oas3VersionFactory = Oas3VersionFactory(this)
  override val vendor: Vendor              = Oas30
  override val syntax: SpecSyntax          = Oas3Syntax
}

