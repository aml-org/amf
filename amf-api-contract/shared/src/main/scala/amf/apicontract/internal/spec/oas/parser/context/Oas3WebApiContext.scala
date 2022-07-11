package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.shapes.internal.spec.common.{OAS30SchemaVersion, SchemaPosition, SchemaVersion}
import amf.shapes.internal.spec.oas.parser
import amf.shapes.internal.spec.oas.parser.Oas3Settings

class Oas3WebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    private val wrapped: ParserContext,
    private val ds: Option[OasWebApiDeclarations] = None,
    options: ParsingOptions = ParsingOptions()
) extends OasWebApiContext(loc, refs, options, wrapped, ds, parser.Oas3Settings(Oas3Syntax)) {
  override val factory: Oas3VersionFactory = Oas3VersionFactory()(this)

  override val defaultSchemaVersion: SchemaVersion = OAS30SchemaVersion.apply(SchemaPosition.Other)

  override def makeCopy(): Oas3WebApiContext =
    new Oas3WebApiContext(rootContextDocument, refs, this, Some(declarations), options)
}
