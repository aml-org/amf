package amf.plugins.parse
import amf.client.remod.amfcore.config.ParsingOptions
import amf.core.Root
import amf.core.exception.InvalidDocumentHeaderException
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParsedReference, ParserContext}
import amf.core.remote.{Oas30, Vendor}
import amf.plugins.document.webapi.contexts.parser.oas.{Oas3WebApiContext, OasWebApiContext}
import amf.plugins.document.webapi.parser.OasHeader
import amf.plugins.document.webapi.parser.OasHeader.Oas30Header
import amf.plugins.document.webapi.parser.spec.OasWebApiDeclarations
import amf.plugins.document.webapi.parser.spec.oas.{Oas3DocumentParser, OasFragmentParser}

object Oas30ParsePlugin extends OasParsePlugin {

  override def vendor: Vendor = Oas30

  override def applies(element: Root): Boolean = OasHeader(element).contains(Oas30Header)

  override def mediaTypes: Seq[String] = Seq(
    "application/oas30+json",
    "application/oas30+yaml",
    "application/oas30",
    "application/openapi30",
    "application/openapi30+yaml",
    "application/openapi30+json"
  )

  override protected def parseSpecificVersion(root: Root)(implicit ctx: OasWebApiContext): BaseUnit =
    OasHeader(root) match {
      case Some(Oas30Header) => Oas3DocumentParser(root).parseDocument()
      case Some(f)           => OasFragmentParser(root, Some(f)).parseFragment()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.name)
    }

  override protected def context(loc: String,
                                 refs: Seq[ParsedReference],
                                 options: ParsingOptions,
                                 wrapped: ParserContext,
                                 ds: Option[OasWebApiDeclarations]): OasWebApiContext =
    new Oas3WebApiContext(loc, refs, wrapped, ds, options)
}
