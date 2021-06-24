package amf.plugins.parse

import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{AntlrParsedDocument, ParserContext, ReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Grpc, Vendor}
import amf.plugins.common.Proto3MediaTypes
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.domain.GrpcPackageParser
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes.SYNTAX
import amf.plugins.document.apicontract.parser.spec.grpc.{AntlrASTParserHelper, GrpcDocumentParser}
import org.mulesoft.antlrast.ast.{Node, Terminal}


object GrpcParsePlugin extends ApiParsePlugin with AntlrASTParserHelper {
  override protected def vendor: Vendor = Grpc

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    GrpcDocumentParser(document)(context(document, ctx)).parseDocument()
  }

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new GrpcReferenceHandler()
  override def mediaTypes: Seq[String] = Proto3MediaTypes.mediaTypes

  override def applies(element: Root): Boolean = {
    element.parsed match {
      case antlrDoc: AntlrParsedDocument =>
        isProto3(antlrDoc)
      case _                             =>
        false
    }
  }

  def isProto3(doc: AntlrParsedDocument): Boolean = {
    path(doc.ast.root(), Seq(SYNTAX)) match {
      case Some(syntaxNode: Node) =>
        syntaxNode.children.exists {
          case t: Terminal => t.value == "\"proto3\""
          case _           => false
        }
      case _               => false
    }
  }

  def context(document: Root, ctx: ParserContext): GrpcWebApiContext = {
    val ast = document.parsed.asInstanceOf[AntlrParsedDocument].ast.root().asInstanceOf[Node]
    val grpcCtx = new GrpcWebApiContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      Some(WebApiDeclarations(Nil, UnhandledErrorHandler,ctx.futureDeclarations))
    )
    // setup the package path
    GrpcPackageParser(ast)(grpcCtx).parseName() match {
      case Some((pkg, _)) => grpcCtx.nestedMessage(pkg)
      case _         => grpcCtx
    }
  }
}
