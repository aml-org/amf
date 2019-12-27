package amf.plugins.document.webapi.contexts.parser.oas
import amf.core.parser.{ParsedReference, ParserContext}
import amf.core.remote.{Oas30, Vendor}
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.plugins.document.webapi.parser.spec.{OasWebApiDeclarations, SpecSyntax}

class Oas3WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        private val wrapped: ParserContext,
                        private val ds: Option[OasWebApiDeclarations] = None)
    extends OasWebApiContext(loc, refs, wrapped, ds) {
  override val factory: Oas3VersionFactory = Oas3VersionFactory()(this)
  override val vendor: Vendor              = Oas30
  override val syntax: SpecSyntax          = Oas3Syntax
}
