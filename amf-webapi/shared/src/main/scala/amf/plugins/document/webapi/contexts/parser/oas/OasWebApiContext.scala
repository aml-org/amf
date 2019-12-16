package amf.plugins.document.webapi.contexts.parser.oas
import amf.core.model.document.ExternalFragment
import amf.core.parser.{ErrorHandler, ParsedReference, ParserContext}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.OasWebApiDeclarations

import scala.collection.mutable

abstract class OasWebApiContext(loc: String,
                                refs: Seq[ParsedReference],
                                private val wrapped: ParserContext,
                                private val ds: Option[OasWebApiDeclarations] = None,
                                parserCount: Option[Int] = None,
                                override val eh: Option[ErrorHandler] = None,
                                private val operationIds: mutable.Set[String] = mutable.HashSet())
    extends OasLikeWebApiContext(loc, refs, wrapped, ds, parserCount, eh) {

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
        errorHandler = Some(this),
        futureDeclarations = futureDeclarations
      ))
}
