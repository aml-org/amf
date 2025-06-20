package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.apicontract.internal.spec.oas.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay, Oas30Header, Oas31Header}
import amf.apicontract.internal.spec.oas.parser.context.{Oas2WebApiContext, OasWebApiContext}
import amf.apicontract.internal.spec.oas.parser.document
import amf.apicontract.internal.spec.oas.parser.document.{Oas2DocumentParser, OasFragmentParser}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.exception.InvalidDocumentHeaderException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Mimes, Oas20, Spec}

object Oas20ParsePlugin extends OasParsePlugin {

  override def spec: Spec = Oas20

  override def applies(element: Root): Boolean = OasHeader(element).exists(h => h != Oas30Header && h != Oas31Header)

  override def mediaTypes: Seq[String] = Seq(Mimes.`application/json`, Mimes.`application/yaml`)

  override protected def parseSpecificVersion(root: Root)(implicit ctx: OasWebApiContext): BaseUnit =
    OasHeader(root) match {
      case Some(Oas20Overlay)   => document.Oas2DocumentParser(root).parseOverlay()
      case Some(Oas20Extension) => Oas2DocumentParser(root).parseExtension()
      case Some(Oas20Header)    => document.Oas2DocumentParser(root).parseDocument()
      case Some(f)              => OasFragmentParser(root, Spec.OAS20, Some(f)).parseFragment()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(spec.id)
    }

  override protected def context(
      loc: String,
      refs: Seq[ParsedReference],
      options: ParsingOptions,
      wrapped: ParserContext,
      ds: Option[OasWebApiDeclarations]
  ): OasWebApiContext =
    new Oas2WebApiContext(loc, refs, wrapped, ds, options)
}
