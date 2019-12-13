package amf.plugins.document.webapi.contexts.parser.oas
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.core.remote.{Vendor, JsonSchema}
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.core.parser.{ParsedReference, ParserContext, ErrorHandler}
import amf.plugins.document.webapi.parser.spec.{OasWebApiDeclarations, SpecSyntax}

class JsonSchemaWebApiContext(loc: String,
                              refs: Seq[ParsedReference],
                              private val wrapped: ParserContext,
                              private val ds: Option[OasWebApiDeclarations],
                              parserCount: Option[Int] = None,
                              override val eh: Option[ErrorHandler] = None)
  extends OasWebApiContext(loc, refs, wrapped, ds, parserCount, eh) {
  override val factory: OasSpecVersionFactory = Oas3VersionFactory(this)
  override val syntax: SpecSyntax             = Oas3Syntax
  override val vendor: Vendor                 = JsonSchema
  override val linkTypes: Boolean = wrapped match {
    case _: RamlWebApiContext => false
    case _: OasWebApiContext  => true // definitions tag
    case _                    => false
  } // oas definitions
}

