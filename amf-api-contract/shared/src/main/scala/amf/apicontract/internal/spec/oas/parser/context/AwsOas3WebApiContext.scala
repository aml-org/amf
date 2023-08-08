package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.{AwsOasWebApiDeclarations, OasWebApiDeclarations}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.ExternalFragment
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.shapes.internal.spec.common.parser.SpecSyntax

class AwsOas3WebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    private val wrapped: ParserContext,
    options: ParsingOptions = ParsingOptions(),
    syntax: SpecSyntax = Oas3Syntax
) extends Oas3WebApiContext(loc, refs, wrapped, None, options, syntax) {
  override val factory: AwsOas3VersionFactory = new AwsOas3VersionFactory()(this)

  override val declarations: AwsOasWebApiDeclarations =
    new AwsOasWebApiDeclarations(
      refs
        .flatMap(r =>
          if (r.isExternalFragment)
            r.unit.asInstanceOf[ExternalFragment].encodes.parsed.map(node => r.origin.url -> node)
          else None
        )
        .toMap,
      None,
      errorHandler = eh,
      futureDeclarations = futureDeclarations
    )
}
