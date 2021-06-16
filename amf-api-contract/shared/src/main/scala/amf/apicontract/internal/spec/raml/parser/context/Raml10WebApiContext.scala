package amf.apicontract.internal.spec.raml.parser.context

import amf.apicontract.internal.spec.common.{ExtensionWebApiDeclarations, RamlWebApiDeclarations}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{Raml10, Vendor}
import amf.shapes.internal.spec.RamlWebApiContextType
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser.SpecSyntax

class Raml10WebApiContext(loc: String,
                          refs: Seq[ParsedReference],
                          override val wrapped: ParserContext,
                          private val ds: Option[RamlWebApiDeclarations] = None,
                          contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
                          options: ParsingOptions = ParsingOptions())
    extends RamlWebApiContext(loc, refs, options, wrapped, ds, contextType) {
  override val factory: RamlSpecVersionFactory = new Raml10VersionFactory()(this)
  override val vendor: Vendor                  = Raml10
  override val syntax: SpecSyntax              = Raml10Syntax

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new Raml10WebApiContext(loc, refs, wrapped, Some(declarations), options = options)
}

class ExtensionLikeWebApiContext(loc: String,
                                 refs: Seq[ParsedReference],
                                 override val wrapped: ParserContext,
                                 val ds: Option[RamlWebApiDeclarations] = None,
                                 val parentDeclarations: RamlWebApiDeclarations,
                                 parserCount: Option[Int] = None,
                                 contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
                                 options: ParsingOptions = ParsingOptions())
    extends Raml10WebApiContext(loc, refs, wrapped, ds, contextType = contextType, options) {

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
                                        errorHandler = eh,
                                        futureDeclarations = futureDeclarations)
    }

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new ExtensionLikeWebApiContext(loc, refs, wrapped, Some(declarations), parentDeclarations, options = options)
}
