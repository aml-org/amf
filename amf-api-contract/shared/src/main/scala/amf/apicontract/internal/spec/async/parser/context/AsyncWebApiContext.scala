package amf.apicontract.internal.spec.async.parser.context

import amf.apicontract.internal.spec.common.AsyncWebApiDeclarations
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.MalformedJsonReference
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.ExternalFragment
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.shapes.internal.spec.common.{JSONSchemaDraft7SchemaVersion, SchemaVersion}

import scala.collection.mutable

abstract class AsyncWebApiContext(loc: String,
                                  refs: Seq[ParsedReference],
                                  options: ParsingOptions,
                                  private val wrapped: ParserContext,
                                  private val ds: Option[AsyncWebApiDeclarations] = None,
                                  private val operationIds: mutable.Set[String] = mutable.HashSet())
    extends OasLikeWebApiContext(loc, refs, options, wrapped, ds, operationIds) {

  override val factory: AsyncSpecVersionFactory

  override val defaultSchemaVersion: SchemaVersion = JSONSchemaDraft7SchemaVersion

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
