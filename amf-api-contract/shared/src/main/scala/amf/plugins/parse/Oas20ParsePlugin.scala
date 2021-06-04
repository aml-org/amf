package amf.plugins.parse

import amf.client.remod.amfcore.config.ParsingOptions
import amf.core.Root
import amf.core.exception.InvalidDocumentHeaderException
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParsedReference, ParserContext}
import amf.core.remote.{Oas20, Vendor}
import amf.plugins.common.Oas20MediaTypes
import amf.plugins.document.apicontract.contexts.parser.oas.{Oas2WebApiContext, OasWebApiContext}
import amf.plugins.document.apicontract.parser.OasHeader
import amf.plugins.document.apicontract.parser.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay, Oas30Header}
import amf.plugins.document.apicontract.parser.spec.OasWebApiDeclarations
import amf.plugins.document.apicontract.parser.spec.oas.{Oas2DocumentParser, Oas3DocumentParser, OasFragmentParser}

object Oas20ParsePlugin extends OasParsePlugin {

  override def vendor: Vendor = Oas20

  override def applies(element: Root): Boolean = OasHeader(element).exists(_ != Oas30Header)

  override def mediaTypes: Seq[String] = Oas20MediaTypes.mediaTypes

  override protected def parseSpecificVersion(root: Root)(implicit ctx: OasWebApiContext): BaseUnit =
    OasHeader(root) match {
      case Some(Oas20Overlay)   => Oas2DocumentParser(root).parseOverlay()
      case Some(Oas20Extension) => Oas2DocumentParser(root).parseExtension()
      case Some(Oas20Header)    => Oas2DocumentParser(root).parseDocument()
      case Some(f)              => OasFragmentParser(root, Some(f)).parseFragment()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.name)
    }

  override protected def context(loc: String,
                                 refs: Seq[ParsedReference],
                                 options: ParsingOptions,
                                 wrapped: ParserContext,
                                 ds: Option[OasWebApiDeclarations]): OasWebApiContext =
    new Oas2WebApiContext(loc, refs, wrapped, ds, options)
}
