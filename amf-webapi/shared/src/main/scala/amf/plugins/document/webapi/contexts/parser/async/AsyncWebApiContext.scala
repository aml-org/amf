package amf.plugins.document.webapi.contexts.parser.async

import amf.core.model.document.ExternalFragment
import amf.core.parser.{ParsedReference, ParserContext}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.AsyncWebApiDeclarations

import scala.collection.mutable

abstract class AsyncWebApiContext(loc: String,
                                  refs: Seq[ParsedReference],
                                  private val wrapped: ParserContext,
                                  private val ds: Option[AsyncWebApiDeclarations] = None,
                                  private val operationIds: mutable.Set[String] = mutable.HashSet())
    extends OasLikeWebApiContext(loc, refs, wrapped, ds, operationIds) {

  override val factory: AsyncSpecVersionFactory

  override val declarations: AsyncWebApiDeclarations =
    ds.getOrElse(
      new AsyncWebApiDeclarations(
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
