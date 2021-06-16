package amf.plugins.parse
import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{AntlrParsedDocument, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Proto3, Vendor}
import amf.plugins.common.Proto3MediaTypes
import amf.plugins.document.apicontract.parser.spec.grpc.GrpcDocumentParser

object GrpcParsePlugin extends ApiParsePlugin {
  override protected def vendor: Vendor = Proto3

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    GrpcDocumentParser(document)(ctx).parseDocument()
  }

  override def mediaTypes: Seq[String] = Proto3MediaTypes.mediaTypes

  override def applies(element: Root): Boolean = {
    element.parsed.isInstanceOf[AntlrParsedDocument] // @todo check the syntax = proto3 is defined
  }
}
