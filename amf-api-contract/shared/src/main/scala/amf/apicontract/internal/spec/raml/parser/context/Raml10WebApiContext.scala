package amf.apicontract.internal.spec.raml.parser.context

import amf.apicontract.internal.spec.common.{ExtensionWebApiDeclarations, RamlWebApiDeclarations}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{Raml10, Spec}
import amf.shapes.internal.spec.RamlWebApiContextType
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.spec.common.parser.SpecSyntax
import org.mulesoft.common.client.lexical.SourceLocation
import amf.shapes.internal.spec.common.parser.SpecSyntax
import amf.shapes.internal.spec.raml.parser.Raml10Settings
import org.yaml.model.{IllegalTypeHandler, ParseErrorHandler, SyamlException, YError}

class Raml10WebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    override val wrapped: ParserContext,
    private val ds: Option[RamlWebApiDeclarations] = None,
    contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
    options: ParsingOptions = ParsingOptions()
) extends RamlWebApiContext(loc, refs, options, wrapped, ds, new Raml10Settings(Raml10Syntax, contextType)) {
  override val factory: RamlSpecVersionFactory = new Raml10VersionFactory()(this)

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new Raml10WebApiContext(loc, refs, wrapped, Some(declarations), options = options)
}

class ExtensionLikeWebApiContext(
    loc: String,
    refs: Seq[ParsedReference],
    override val wrapped: ParserContext,
    val ds: Option[RamlWebApiDeclarations] = None,
    val parentDeclarations: RamlWebApiDeclarations,
    parserCount: Option[Int] = None,
    contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
    options: ParsingOptions = ParsingOptions()
) extends Raml10WebApiContext(loc, refs, wrapped, ds, contextType = contextType, options) {

  override val declarations: ExtensionWebApiDeclarations =
    ds match {
      case Some(dec) =>
        new ExtensionWebApiDeclarations(
          parentDeclarations,
          dec.alias,
          dec.errorHandler,
          dec.futureDeclarations
        )
      case None =>
        new ExtensionWebApiDeclarations(
          parentDeclarations = parentDeclarations,
          alias = None,
          errorHandler = eh,
          futureDeclarations = futureDeclarations
        )
    }

  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext =
    new ExtensionLikeWebApiContext(loc, refs, wrapped, Some(declarations), parentDeclarations, options = options)
}
