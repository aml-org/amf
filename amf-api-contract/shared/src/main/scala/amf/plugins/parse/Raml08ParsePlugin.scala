package amf.plugins.parse

import amf.client.remod.amfcore.config.ParsingOptions
import amf.core.Root
import amf.core.exception.InvalidDocumentHeaderException
import amf.core.model.document.BaseUnit
import amf.core.parser.{EmptyFutureDeclarations, LinkReference, ParserContext}
import amf.core.remote.{Raml10, Vendor}
import amf.plugins.common.Raml08MediaTypes
import amf.plugins.document.apicontract.contexts.parser.raml.{
  Raml08WebApiContext,
  Raml10WebApiContext,
  RamlWebApiContext
}
import amf.plugins.document.apicontract.parser.RamlFragmentHeader._
import amf.plugins.document.apicontract.parser.{RamlFragment, RamlHeader}
import amf.plugins.document.apicontract.parser.RamlHeader.{
  Raml08,
  Raml10,
  Raml10Extension,
  Raml10Library,
  Raml10Overlay
}
import amf.plugins.document.apicontract.parser.spec.raml.{
  ExtensionLikeParser,
  Raml08DocumentParser,
  Raml10DocumentParser,
  RamlModuleParser
}
import amf.plugins.document.apicontract.parser.spec.{RamlWebApiDeclarations, WebApiDeclarations}

object Raml08ParsePlugin extends RamlParsePlugin {

  override def vendor: Vendor = Vendor.RAML08

  override def applies(element: Root): Boolean = RamlHeader(element) exists {
    // Partial raml0.8 fragment with RAML header but linked through !include
    // we need to generate an external fragment and inline it in the parent document
    case Raml08 if element.referenceKind != LinkReference => true
    case _: RamlFragment                                  => false
    case _                                                => false
  }

  override def mediaTypes: Seq[String] = Raml08MediaTypes.mediaTypes

  override def context(wrapped: ParserContext,
                       root: Root,
                       options: ParsingOptions,
                       ds: Option[WebApiDeclarations]): RamlWebApiContext =
    new Raml08WebApiContext(root.location,
                            root.references ++ wrapped.refs,
                            wrapped,
                            ds.map(d => RamlWebApiDeclarations(d)),
                            options = options)

  override protected def parseSpecificVersion(root: Root, ctx: RamlWebApiContext, header: RamlHeader): BaseUnit = {
    header match {
      case Raml08 => Raml08DocumentParser(root)(ctx).parseDocument()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.name)
    }
  }
}
