package amf.plugins.document.apicontract.parser.spec.grpc

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.parse.document.{AntlrParsedDocument, ParserContext}
import amf.core.internal.parser.Root
import amf.plugins.document.apicontract.parser.spec.domain.{GrpcMessageParser, GrpcPackageParser, GrpcServiceParser}
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import org.mulesoft.antlrast.ast.{ASTElement, Node}

case class GrpcDocumentParser(root: Root)(implicit val ctx: ParserContext)extends AntlrASTParserHelper  {

  val doc: Document = Document()

  def parseDocument(): Document = {
    val ast = root.parsed.asInstanceOf[AntlrParsedDocument].ast
    ast.root() match {
      case node: Node =>
        parseMessages(node)
        parseWebAPI(node)
    }
    doc
  }

  def parseWebAPI(node: Node): Unit = {
    val webApi = GrpcPackageParser(node).parse()
    doc.adopted(root.location).withLocation(root.location).withEncodes(webApi)
    parseServices(node)
  }

  def parseMessages(node: Node): Unit = {
    collect(node, Seq(TOP_LEVEL_DEF, MESSAGE_DEF)).foreach { element: ASTElement =>
      withNode(element) { node =>
        val shape = GrpcMessageParser(node).parse()
        shape.withId(doc.location() + s"#/declarations/${shape.name.value()}")
        doc.withDeclares(doc.declares ++ Seq(shape))
      }
    }
  }

  def parseServices(node: Node): Unit = {
    val webApi = doc.encodes.asInstanceOf[WebApi]
    val endPoints: Seq[EndPoint] = collect(node, Seq(TOP_LEVEL_DEF, SERVICE_DEF)).zipWithIndex.map { case (element: ASTElement, idx: Int) =>
      withNode(element) { node =>
        GrpcServiceParser(node).parse(idx)
      }
    }
    webApi.withEndPoints(endPoints)
  }

}
