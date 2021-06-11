package amf.plugins.document.apicontract.contexts.parser.oas

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.ExternalFragment
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{JsonSchema, Vendor}
import amf.plugins.document.apicontract.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.apicontract.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.apicontract.parser.spec.declaration.JSONSchemaVersion
import amf.plugins.document.apicontract.parser.spec.oas.Oas3Syntax
import amf.plugins.document.apicontract.parser.spec.{OasWebApiDeclarations, SpecSyntax}

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
