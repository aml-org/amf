package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.apicontract.internal.spec.oas.OasHeader.Oas31Header
import amf.apicontract.internal.spec.oas.parser.context.{Oas31WebApiContext, OasWebApiContext}
import amf.apicontract.internal.spec.oas.parser.document.{Oas31DocumentParser, OasFragmentParser}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.exception.InvalidDocumentHeaderException
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Oas31, Spec}

object Oas31ParsePlugin extends OasParsePlugin {

  override def spec: Spec = Oas31

  override def applies(element: Root): Boolean =
    OasHeader(element).contains(Oas31Header)

  override def mediaTypes: Seq[String] = Seq()

  override protected def parseSpecificVersion(root: Root)(implicit ctx: OasWebApiContext): BaseUnit =
    OasHeader(root) match {
      case Some(Oas31Header) => Oas31DocumentParser(root).parseDocument()
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
    new Oas31WebApiContext(loc, refs, wrapped, ds, options)
}
