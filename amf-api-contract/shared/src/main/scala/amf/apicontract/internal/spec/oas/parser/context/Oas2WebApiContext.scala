package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{Oas20, Vendor}

class Oas2WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        wrapped: ParserContext,
                        ds: Option[OasWebApiDeclarations] = None,
                        options: ParsingOptions = ParsingOptions())
    extends OasWebApiContext(loc, refs, options, wrapped, ds) {
  override val factory: Oas2VersionFactory = Oas2VersionFactory()(this)
  override val vendor: Vendor              = Oas20
  override val syntax: SpecSyntax          = Oas2Syntax

  override def makeCopy(): Oas2WebApiContext =
    new Oas2WebApiContext(rootContextDocument, refs, this, Some(declarations), options)
}
