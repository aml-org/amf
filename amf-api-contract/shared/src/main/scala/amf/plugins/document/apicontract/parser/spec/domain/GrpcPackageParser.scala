package amf.plugins.document.apicontract.parser.spec.domain

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.parse.document.ParserContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import org.mulesoft.antlrast.ast.{ASTElement, Node}

class GrpcPackageParser(ast: Node)(implicit val ctx: ParserContext) extends AntlrASTParserHelper {
  val webApi  = WebApi()

  def parse(): WebApi = {
    parseName()
    webApi
  }

  private def parseName(): Unit = {
    val ids: Seq[String] = collect(ast, Seq(PACKAGE_STATEMENT, FULL_IDENTIFIER, IDENTIFIER)).map { element: ASTElement =>
      withOptTerminal(element) {
        case Some(packageId) =>
          packageId.value
        case None            =>
          ""
      }
    }
    if (ids.nonEmpty) {
      webApi.withName(ids.mkString("."))
    } else {
      webApi.withName(ctx.rootContextDocument.split("/").last)
    }
  }

}

object GrpcPackageParser {
  def apply(ast: Node)(implicit ctx: ParserContext): GrpcPackageParser = new GrpcPackageParser(ast)
}