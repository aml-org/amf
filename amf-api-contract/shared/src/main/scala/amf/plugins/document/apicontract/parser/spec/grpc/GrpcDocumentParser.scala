package amf.plugins.document.apicontract.parser.spec.grpc

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.parse.document.AntlrParsedDocument
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.Root
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.domain.{GrpcMessageParser, GrpcPackageParser, GrpcServiceParser}
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import org.mulesoft.antlrast.ast.{ASTElement, Node}

case class GrpcDocumentParser(root: Root)(implicit val ctx: GrpcWebApiContext)extends AntlrASTParserHelper  {

  val doc: Document = Document()

  def parseDocument(): Document = {
    val ast = root.parsed.asInstanceOf[AntlrParsedDocument].ast
    ast.root() match {
      case node: Node =>
        parseWebAPI(node)
        parseMessages(node)
        parseServices(node)
    }
    ctx.declarations.futureDeclarations.resolve()
    doc.withDeclares(ctx.declarations.shapes.values.toList)
  }

  def parseWebAPI(node: Node): Unit = {
    val webApi = GrpcPackageParser(node).parse()
    doc.adopted(root.location).withLocation(root.location).withEncodes(webApi)
  }

  def webapi: WebApi = doc.encodes.asInstanceOf[WebApi]

  def parseMessages(node: Node): Unit = {
    collect(node, Seq(TOP_LEVEL_DEF, MESSAGE_DEF)).zipWithIndex.foreach { case (element: ASTElement, idx: Int) =>
      withNode(element) { node =>
        val shape = GrpcMessageParser(node).parse(shape => {
          shape.name.option() match {
            case None => shape.withName(s"Message${idx}")
            case _    =>
          }
          shape.adopted(webapi.id + "/types")
        })
        ctx.declarations += shape.add(DeclaredElement())
      }
    }
  }

  def parseServices(node: Node): Unit = {
    val webApi = doc.encodes.asInstanceOf[WebApi]
    val endPoints: Seq[EndPoint] = collect(node, Seq(TOP_LEVEL_DEF, SERVICE_DEF)).zipWithIndex.map { case (element: ASTElement, idx: Int) =>
      withNode(element) { node =>
        GrpcServiceParser(node).parse(ep => webApi.withEndPoints(webApi.endPoints ++ Seq(ep)))
      }
    }
    webApi.withEndPoints(endPoints)
  }

}
