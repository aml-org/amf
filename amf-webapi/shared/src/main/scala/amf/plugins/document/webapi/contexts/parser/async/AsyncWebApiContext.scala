package amf.plugins.document.webapi.contexts.parser.async

import amf.core.client.ParsingOptions
import amf.core.model.document.ExternalFragment
import amf.core.parser.{ParsedReference, ParserContext}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.AsyncWebApiDeclarations
import amf.validations.ParserSideValidations.MalformedJsonReference

import scala.collection.mutable

abstract class AsyncWebApiContext(loc: String,
                                  refs: Seq[ParsedReference],
                                  options: ParsingOptions,
                                  private val wrapped: ParserContext,
                                  private val ds: Option[AsyncWebApiDeclarations] = None,
                                  private val operationIds: mutable.Set[String] = mutable.HashSet())
    extends OasLikeWebApiContext(loc, refs, options, wrapped, ds, operationIds) {

  override val factory: AsyncSpecVersionFactory

  override def validateRefFormatWithError(ref: String): Boolean = {
    if (ref.contains("#") && !ref.contains("#/")) {
      eh.violation(MalformedJsonReference, loc, s"AsyncApi JSONReference '${ref}' should use '#/', not '#' only")
      false
    } else true
  }

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
