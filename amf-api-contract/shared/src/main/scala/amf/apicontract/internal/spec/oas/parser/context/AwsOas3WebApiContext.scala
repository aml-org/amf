package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.shapes.internal.spec.common.parser.SpecSyntax

class AwsOas3WebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    private val wrapped: ParserContext,
    private val ds: Option[OasWebApiDeclarations] = None,
    options: ParsingOptions = ParsingOptions(),
    syntax: SpecSyntax = Oas3Syntax
) extends Oas3WebApiContext(loc, refs, wrapped, ds, options, syntax) {
  override val factory: AwsOas3VersionFactory = new AwsOas3VersionFactory()(this)
}
