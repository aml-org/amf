package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.ExternalFragment
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}

abstract class OasWebApiContext(loc: String,
                                refs: Seq[ParsedReference],
                                options: ParsingOptions,
                                private val wrapped: ParserContext,
                                private val ds: Option[OasWebApiDeclarations] = None)
    extends OasLikeWebApiContext(loc, refs, options, wrapped, ds) {

  override val factory: OasSpecVersionFactory

  override val declarations: OasWebApiDeclarations =
    ds.getOrElse(
      new OasWebApiDeclarations(
        refs
          .flatMap(
            r =>
              if (r.isExternalFragment)
                r.unit.asInstanceOf[ExternalFragment].encodes.parsed.map(node => r.origin.url -> node)
              else None)
          .toMap,
        None,
        errorHandler = eh,
        futureDeclarations = futureDeclarations
      ))
}
