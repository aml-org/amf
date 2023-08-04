package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.apicontract.internal.spec.oas.OasHeader.Oas30Header
import amf.apicontract.internal.spec.oas.parser.context.{AwsOas3WebApiContext, OasWebApiContext}
import amf.apicontract.internal.spec.oas.parser.document.AwsOas3DocumentParser
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AwsOas30, Spec}

object AwsOas30ParsePlugin extends OasParsePlugin {

  override def spec: Spec = AwsOas30

  override def applies(element: Root): Boolean = OasHeader(element).contains(Oas30Header)

  override def mediaTypes: Seq[String] = Seq.empty

  override protected def parseSpecificVersion(root: Root)(implicit ctx: OasWebApiContext): BaseUnit =
    new AwsOas3DocumentParser(root).parseDocument()

  override protected def context(
      loc: String,
      refs: Seq[ParsedReference],
      options: ParsingOptions,
      wrapped: ParserContext,
      ds: Option[OasWebApiDeclarations]
  ): OasWebApiContext =
    new AwsOas3WebApiContext(loc, refs, wrapped, ds, options)
}
