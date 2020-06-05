package amf.plugins.document.webapi.contexts.parser.raml
import amf.core.client.ParsingOptions
import amf.core.parser.{ParsedReference, ParserContext}
import amf.core.remote.{Raml08, Vendor}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContextType.RamlWebApiContextType
import amf.plugins.document.webapi.parser.spec.raml.Raml08Syntax
import amf.plugins.document.webapi.parser.spec.{RamlWebApiDeclarations, SpecSyntax}

class Raml08WebApiContext(loc: String,
                          refs: Seq[ParsedReference],
                          override val wrapped: ParserContext,
                          private val ds: Option[RamlWebApiDeclarations] = None,
                          contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
                          options: ParsingOptions = ParsingOptions())
    extends RamlWebApiContext(loc, refs, options, wrapped, ds, contextType) {
  override val factory: RamlSpecVersionFactory = new Raml08VersionFactory()(this)
  override val vendor: Vendor                  = Raml08
  override val syntax: SpecSyntax              = Raml08Syntax

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new Raml08WebApiContext(loc, refs, wrapped, Some(declarations), options = options)

  override protected def supportsAnnotations: Boolean = false
}
