package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.shapes.internal.spec.common.parser.SpecSyntax
import amf.shapes.internal.spec.common.{OAS31SchemaVersion, SchemaPosition, SchemaVersion}
import amf.shapes.internal.spec.oas.parser

class Oas31WebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    private val wrapped: ParserContext,
    private val ds: Option[OasWebApiDeclarations] = None,
    options: ParsingOptions = ParsingOptions(),
    syntax: SpecSyntax = Oas3Syntax
) extends OasWebApiContext(loc, refs, options, wrapped, ds, parser.Oas3Settings(syntax)) {
  override val factory: Oas31VersionFactory = Oas31VersionFactory()(this)

  override val defaultSchemaVersion: SchemaVersion = OAS31SchemaVersion.apply(SchemaPosition.Other)

  override def makeCopy(): Oas31WebApiContext =
    new Oas31WebApiContext(rootContextDocument, refs, this, Some(declarations), options)
}
