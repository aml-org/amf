package amf.plugins.document.apicontract.parser.spec.domain

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.internal.parser.domain.Annotations
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import org.mulesoft.antlrast.ast.{ASTElement, Node}

class GrpcPackageParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends AntlrASTParserHelper {
  val webApi  = WebApi()

  def parse(): WebApi = {
    parseName() match {
      case Some((pkg, annotations)) => webApi.withName(pkg, annotations)
      case _         =>
        astError(webApi.id, "Missing protobuf3 package statement", toAnnotations(ast))
        webApi.withName(ctx.rootContextDocument.split("/").last)
    }
    collectOptions(ast, { extension =>
      extension.adopted(webApi.id)
      webApi.withCustomDomainProperty(extension)
    })
    webApi
  }

  def parseName(): Option[(String, Annotations)] = {
    path(ast, Seq(PACKAGE_STATEMENT)) match {
      case Some(n: Node) =>
        val ids: Seq[String] = collect(n, Seq(FULL_IDENTIFIER, IDENTIFIER)).map { element: ASTElement =>
          withOptTerminal(element) {
            case Some(packageId) =>
              packageId.value
            case None            =>
              ""
          }
        }
        if (ids.nonEmpty) {
          Some(ids.mkString("."), toAnnotations(n))
        } else {
          None
        }
      case _ => None
    }
  }

}

object GrpcPackageParser {
  def apply(ast: Node)(implicit ctx: GrpcWebApiContext): GrpcPackageParser = new GrpcPackageParser(ast)
}