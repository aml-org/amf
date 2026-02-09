package amf.grpc.internal.spec.parser.domain

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.core.client.scala.model.document.Document
import amf.core.internal.parser.domain.Annotations
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.{ASTNode, Node}

class GrpcPackageParser(ast: Node, doc: Document)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val ann: Annotations = toAnnotations(ast)
  val webApi: WebApi   = WebApi(ann)

  def parse(): WebApi = {
    // validate multiple package statements
    if (collect(ast, Seq(PACKAGE_STATEMENT)).size > 1) {
      astError("Multiple protobuf3 package statements", ann)
    }
    parseName() match {
      case Some((pkg, annotations)) =>
        doc.withPkg(pkg, annotations)
        webApi.withName(pkg, annotations)
      case _ =>
        // package statement is not required (protoc doesn't consider it as an error)
        // astError("Missing protobuf3 package statement", ann)
        doc.withPkg("default", Annotations.synthesized())
        webApi.withName(ctx.rootContextDocument.split("/").last, Annotations.synthesized())
    }
    collectOptions(
      ast,
      Seq(OPTION_STATEMENT),
      extension => {
        val extensions = webApi.customDomainProperties :+ extension
        webApi set (extensions, ann) as WebApiModel.CustomDomainProperties
      }
    )
    webApi
  }

  def parseName(): Option[(String, Annotations)] = {
    path(ast, Seq(PACKAGE_STATEMENT)) match {
      case Some(n: Node) =>
        val ids: Seq[String] = collect(n, Seq(FULL_IDENTIFIER, IDENTIFIER)).map { element: ASTNode =>
          withOptTerminal(element) {
            case Some(packageId) => packageId.value
            case None            => ""
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
