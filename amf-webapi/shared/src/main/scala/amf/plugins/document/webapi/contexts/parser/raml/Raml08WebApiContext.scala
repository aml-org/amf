package amf.plugins.document.webapi.contexts.parser.raml
import amf.plugins.document.webapi.parser.spec.{SpecSyntax, RamlWebApiDeclarations}
import amf.plugins.document.webapi.parser.spec.raml.Raml08Syntax
import amf.core.parser.{ErrorHandler, ParserContext, ParsedReference}
import amf.core.remote.{Raml08, Vendor}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContextType.RamlWebApiContextType

class Raml08WebApiContext(loc: String,
                          refs: Seq[ParsedReference],
                          override val wrapped: ParserContext,
                          private val ds: Option[RamlWebApiDeclarations] = None,
                          parserCount: Option[Int] = None,
                          override val eh: Option[ErrorHandler] = None,
                          contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT)
  extends RamlWebApiContext(loc, refs, wrapped, ds, parserCount, eh, contextType) {
  override val factory: RamlSpecVersionFactory = new Raml08VersionFactory()(this)
  override val vendor: Vendor                  = Raml08
  override val syntax: SpecSyntax              = Raml08Syntax

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new Raml08WebApiContext(loc, refs, wrapped, Some(declarations), eh = eh)

  override protected def supportsAnnotations: Boolean = false
}