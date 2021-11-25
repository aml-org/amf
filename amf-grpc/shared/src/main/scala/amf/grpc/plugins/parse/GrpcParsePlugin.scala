package amf.grpc.plugins.parse

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.parse.document.{ParserContext, ReferenceHandler}
import amf.core.internal.parser.Root
import amf.core.internal.remote.{Grpc, Spec, Syntax}
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.document.GrpcDocumentParser
import amf.grpc.internal.spec.parser.domain.GrpcPackageParser
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.SYNTAX
import org.mulesoft.antlrast.ast.{Node, Terminal}

object GrpcParsePlugin extends ApiParsePlugin with GrpcASTParserHelper {

  override def spec: Spec = Grpc

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    GrpcDocumentParser(document)(context(document, ctx)).parseDocument()
  }

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new GrpcReferenceHandler()

  override def mediaTypes: Seq[String] = Syntax.proto3Mimes.toSeq

  override def applies(element: Root): Boolean = {
    element.parsed match {
      case antlrDoc: AntlrParsedDocument =>
        isProto3(antlrDoc)
      case _ =>
        false
    }
  }

  private def isProto3(doc: AntlrParsedDocument): Boolean = {
    path(doc.ast.root(), Seq(SYNTAX)) match {
      case Some(syntaxNode: Node) =>
        syntaxNode.children.exists {
          case t: Terminal => t.value == "\"proto3\""
          case _           => false
        }
      case _ => false
    }
  }

  def context(document: Root, ctx: ParserContext): GrpcWebApiContext = {
    val ast = document.parsed.asInstanceOf[AntlrParsedDocument].ast.root().asInstanceOf[Node]
    val grpcCtx = new GrpcWebApiContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx.parsingOptions,
      ctx,
      Some(WebApiDeclarations(Nil, UnhandledErrorHandler, ctx.futureDeclarations))
    )
    // setup the package path
    GrpcPackageParser(ast, Document())(grpcCtx).parseName() match {
      case Some((pkg, _)) => grpcCtx.nestedMessage(pkg)
      case _              => grpcCtx
    }
  }

  override def withIdAdoption: Boolean = false // TODO pending analysis and parsing cleanup
}
