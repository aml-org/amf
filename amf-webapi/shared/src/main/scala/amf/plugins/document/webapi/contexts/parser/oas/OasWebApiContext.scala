package amf.plugins.document.webapi.contexts.parser.oas
import amf.core.client.ParsingOptions
import amf.core.model.document.ExternalFragment
import amf.core.parser.{ParsedReference, ParserContext}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.OasWebApiDeclarations

import scala.collection.mutable

abstract class OasWebApiContext(loc: String,
                                refs: Seq[ParsedReference],
                                options: ParsingOptions,
                                private val wrapped: ParserContext,
                                private val ds: Option[OasWebApiDeclarations] = None,
                                private val operationIds: mutable.Set[String] = mutable.HashSet())
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
