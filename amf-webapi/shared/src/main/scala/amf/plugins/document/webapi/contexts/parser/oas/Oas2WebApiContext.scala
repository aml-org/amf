package amf.plugins.document.webapi.contexts.parser.oas
import amf.core.parser.{ParsedReference, ParserContext}
import amf.core.remote.{Oas20, Vendor}
import amf.plugins.document.webapi.parser.spec.oas.Oas2Syntax
import amf.plugins.document.webapi.parser.spec.{OasWebApiDeclarations, SpecSyntax}

class Oas2WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        private val wrapped: ParserContext,
                        private val ds: Option[OasWebApiDeclarations] = None)
    extends OasWebApiContext(loc, refs, wrapped, ds) {
  override val factory: Oas2VersionFactory = Oas2VersionFactory()(this)
  override val vendor: Vendor              = Oas20
  override val syntax: SpecSyntax          = Oas2Syntax
}
