package amf.plugins.document.apicontract.contexts.parser.oas

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{Oas30, Vendor}
import amf.plugins.document.apicontract.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.apicontract.parser.spec.declaration.JSONSchemaVersion
import amf.plugins.document.apicontract.parser.spec.oas.Oas3Syntax
import amf.plugins.document.apicontract.parser.spec.{OasWebApiDeclarations, SpecSyntax}

class Oas3WebApiContext(loc: String,
                        refs: Seq[ParsedReference],
                        private val wrapped: ParserContext,
                        private val ds: Option[OasWebApiDeclarations] = None,
                        options: ParsingOptions = ParsingOptions())
    extends OasWebApiContext(loc, refs, options, wrapped, ds) {
  override val factory: Oas3VersionFactory = Oas3VersionFactory()(this)
  override val vendor: Vendor              = Oas30
  override val syntax: SpecSyntax          = Oas3Syntax

  override def makeCopy(): Oas3WebApiContext =
    new Oas3WebApiContext(rootContextDocument, refs, this, Some(declarations), options)
}
