package amf.plugins.document.webapi.contexts.parser.oas
import amf.plugins.document.webapi.parser.spec.oas.Oas2Syntax
import amf.core.parser.{ParserContext, ErrorHandler, ParsedReference}
import amf.plugins.document.webapi.parser.spec.{OasWebApiDeclarations, SpecSyntax}
import amf.core.remote.{Vendor, Oas20}

class Oas2WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        private val wrapped: ParserContext,
                        private val ds: Option[OasWebApiDeclarations] = None,
                        parserCount: Option[Int] = None,
                        override val eh: Option[ErrorHandler] = None)
  extends OasWebApiContext(loc, refs, wrapped, ds, parserCount, eh) {
  override val factory: Oas2VersionFactory = Oas2VersionFactory(this)
  override val vendor: Vendor              = Oas20
  override val syntax: SpecSyntax          = Oas2Syntax
}
