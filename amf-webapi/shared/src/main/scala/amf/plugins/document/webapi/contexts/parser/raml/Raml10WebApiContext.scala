package amf.plugins.document.webapi.contexts.parser.raml
import amf.plugins.document.webapi.parser.spec.raml.Raml10Syntax
import amf.core.parser.{ErrorHandler, ParserContext, ParsedReference}
import amf.core.remote.{Vendor, Raml10}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContextType.RamlWebApiContextType
import amf.plugins.document.webapi.parser.spec.{SpecSyntax, RamlWebApiDeclarations, ExtensionWebApiDeclarations}

class Raml10WebApiContext(loc: String,
                          refs: Seq[ParsedReference],
                          override val wrapped: ParserContext,
                          private val ds: Option[RamlWebApiDeclarations] = None,
                          parserCount: Option[Int] = None,
                          override val eh: Option[ErrorHandler] = None,
                          contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT)
  extends RamlWebApiContext(loc, refs, wrapped, ds, parserCount, eh, contextType) {
  override val factory: RamlSpecVersionFactory = new Raml10VersionFactory()(this)
  override val vendor: Vendor                  = Raml10
  override val syntax: SpecSyntax              = Raml10Syntax

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new Raml10WebApiContext(loc, refs, wrapped, Some(declarations), eh = eh)
}

class ExtensionLikeWebApiContext(loc: String,
                                 refs: Seq[ParsedReference],
                                 override val wrapped: ParserContext,
                                 val ds: Option[RamlWebApiDeclarations] = None,
                                 val parentDeclarations: RamlWebApiDeclarations,
                                 parserCount: Option[Int] = None,
                                 contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT)
  extends Raml10WebApiContext(loc, refs, wrapped, ds, parserCount = parserCount, contextType = contextType) {

  override val declarations: ExtensionWebApiDeclarations =
    ds match {
      case Some(dec) =>
        new ExtensionWebApiDeclarations(dec.externalShapes,
          dec.externalLibs,
          parentDeclarations,
          dec.alias,
          dec.errorHandler,
          dec.futureDeclarations)
      case None =>
        new ExtensionWebApiDeclarations(parentDeclarations = parentDeclarations,
          alias = None,
          errorHandler = Some(this),
          futureDeclarations = futureDeclarations)
    }

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new ExtensionLikeWebApiContext(loc, refs, wrapped, Some(declarations), parentDeclarations)
}