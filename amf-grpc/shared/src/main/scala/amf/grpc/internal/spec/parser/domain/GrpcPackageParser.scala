package amf.grpc.internal.spec.parser.domain

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.model.document.Document
import amf.core.internal.parser.domain.Annotations
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.{ASTNode, Node}

class GrpcPackageParser(ast: Node, doc: Document)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val webApi = WebApi()

  def parse(): WebApi = {
    parseName() match {
      case Some((pkg, annotations)) =>
        doc.withPkg(pkg, annotations)
        webApi.withName(pkg, annotations)
      case _ =>
        astError(webApi.id, "Missing protobuf3 package statement", toAnnotations(ast))
        webApi.withName(ctx.rootContextDocument.split("/").last)
    }
    collectOptions(
      ast,
      Seq(OPTION_STATEMENT),
      { extension =>
        extension.adopted(webApi.id)
        webApi.withCustomDomainProperty(extension)
      }
    )
    webApi
  }

  def parseName(): Option[(String, Annotations)] = {
    path(ast, Seq(PACKAGE_STATEMENT)) match {
      case Some(n: Node) =>
        val ids: Seq[String] = collect(n, Seq(FULL_IDENTIFIER, IDENTIFIER)).map { element: ASTNode =>
          withOptTerminal(element) {
            case Some(packageId) =>
              packageId.value
            case None =>
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
  def apply(ast: Node, doc: Document)(implicit ctx: GrpcWebApiContext): GrpcPackageParser =
    new GrpcPackageParser(ast, doc)
}
