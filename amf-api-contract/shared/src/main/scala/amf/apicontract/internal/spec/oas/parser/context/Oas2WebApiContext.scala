package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{Oas20, Spec}
import amf.shapes.internal.spec.common.parser.{Oas2Settings, SpecSyntax}
import amf.shapes.internal.spec.common.{OAS20SchemaVersion, SchemaPosition, SchemaVersion}

class Oas2WebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    wrapped: ParserContext,
    ds: Option[OasWebApiDeclarations] = None,
    options: ParsingOptions = ParsingOptions()
) extends OasWebApiContext(loc, refs, options, wrapped, ds, Oas2Settings(Oas2Syntax)) {
  override val factory: Oas2VersionFactory = Oas2VersionFactory()(this)

  override val defaultSchemaVersion: SchemaVersion = OAS20SchemaVersion.apply(SchemaPosition.Other)

  override def makeCopy(): Oas2WebApiContext =
    new Oas2WebApiContext(rootContextDocument, refs, this, Some(declarations), options)
}
