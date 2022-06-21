package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{Oas20, Spec}
import amf.shapes.internal.spec.common.parser.SpecSyntax
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, SchemaPosition, SchemaVersion}

class Oas2WebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    wrapped: ParserContext,
    ds: Option[OasWebApiDeclarations] = None,
    options: ParsingOptions = ParsingOptions()
) extends OasWebApiContext(loc, refs, options, wrapped, ds) {
  override val factory: Oas2VersionFactory = Oas2VersionFactory()(this)
  override def spec: Spec                  = Oas20
  override def syntax: SpecSyntax          = Oas2Syntax

  override val defaultSchemaVersion: SchemaVersion = OAS20SchemaVersion.apply(SchemaPosition.Other)

  override def makeCopy(): Oas2WebApiContext =
    new Oas2WebApiContext(rootContextDocument, refs, this, Some(declarations), options)
}
