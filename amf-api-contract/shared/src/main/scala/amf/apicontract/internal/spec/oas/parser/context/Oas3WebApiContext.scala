package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{Oas30, Spec}
import amf.shapes.internal.spec.common.parser.SpecSyntax

class Oas3WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        private val wrapped: ParserContext,
                        private val ds: Option[OasWebApiDeclarations] = None,
                        options: ParsingOptions = ParsingOptions())
    extends OasWebApiContext(loc, refs, options, wrapped, ds) {
  override val factory: Oas3VersionFactory = Oas3VersionFactory()(this)
  override val vendor: Spec                = Oas30
  override val syntax: SpecSyntax          = Oas3Syntax

  override def makeCopy(): Oas3WebApiContext =
    new Oas3WebApiContext(rootContextDocument, refs, this, Some(declarations), options)
}
