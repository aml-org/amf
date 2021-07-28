package amf.apicontract.internal.spec.jsonschema

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.apicontract.internal.spec.oas.parser.context.{
  Oas3Syntax,
  Oas3VersionFactory,
  OasSpecVersionFactory,
  OasWebApiContext
}
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{JsonSchema, SpecId}
import amf.shapes.internal.spec.common.JSONSchemaVersion
import amf.shapes.internal.spec.common.parser.SpecSyntax

class JsonSchemaWebApiContext(loc: String,
                              refs: Seq[ParsedReference],
                              private val wrapped: ParserContext,
                              private val ds: Option[OasWebApiDeclarations],
                              options: ParsingOptions = ParsingOptions(),
                              override val defaultSchemaVersion: JSONSchemaVersion)
    extends OasWebApiContext(loc, refs, options, wrapped, ds) {

  override val factory: OasSpecVersionFactory = Oas3VersionFactory()(this)
  override val syntax: SpecSyntax             = Oas3Syntax
  override val vendor: SpecId                 = JsonSchema
  override val linkTypes: Boolean = wrapped match {
    case _: RamlWebApiContext => false
    case _: OasWebApiContext  => true // definitions tag
    case _                    => false
  } // oas definitions

  override def makeCopy(): JsonSchemaWebApiContext =
    new JsonSchemaWebApiContext(rootContextDocument, refs, this, Some(declarations), options, defaultSchemaVersion)
}
