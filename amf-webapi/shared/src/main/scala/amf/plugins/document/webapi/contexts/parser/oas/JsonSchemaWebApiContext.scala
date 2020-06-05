package amf.plugins.document.webapi.contexts.parser.oas
import amf.core.client.ParsingOptions
import amf.core.parser.{ParsedReference, ParserContext}
import amf.core.remote.{JsonSchema, Vendor}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.plugins.document.webapi.parser.spec.{OasWebApiDeclarations, SpecSyntax}

class JsonSchemaWebApiContext(loc: String,
                              refs: Seq[ParsedReference],
                              private val wrapped: ParserContext,
                              private val ds: Option[OasWebApiDeclarations],
                              options: ParsingOptions = ParsingOptions())
    extends OasWebApiContext(loc, refs, options, wrapped, ds) {
  override val factory: OasSpecVersionFactory = Oas3VersionFactory()(this)
  override val syntax: SpecSyntax             = Oas3Syntax
  override val vendor: Vendor                 = JsonSchema
  override val linkTypes: Boolean = wrapped match {
    case _: RamlWebApiContext => false
    case _: OasWebApiContext  => true // definitions tag
    case _                    => false
  } // oas definitions

  override def makeCopy(): JsonSchemaWebApiContext =
    new JsonSchemaWebApiContext(rootContextDocument, refs, this, Some(declarations), options)
}
