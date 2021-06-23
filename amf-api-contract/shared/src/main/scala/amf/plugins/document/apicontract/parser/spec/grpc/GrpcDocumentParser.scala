package amf.plugins.document.apicontract.parser.spec.grpc

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.model.document.{DeclaresModel, Document}
import amf.core.client.scala.parse.document.{AntlrParsedDocument, ParsedReference}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.Root
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.domain.{GrpcEnumParser, GrpcMessageParser, GrpcPackageParser, GrpcServiceParser}
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import amf.shapes.client.scala.model.domain.AnyShape
import org.mulesoft.antlrast.ast.{ASTElement, Node}

case class GrpcDocumentParser(root: Root)(implicit val ctx: GrpcWebApiContext)extends AntlrASTParserHelper  {

  val doc: Document = Document()

  def loadReferences(references: Seq[ParsedReference]): Unit = {
    references.foreach { reference =>
      reference.unit match {
        case dec: DeclaresModel =>
          dec.declares.foreach { case shape: AnyShape =>
            ctx.globalSpace += (shape.name.value() -> shape)
          }
        case _                  => // ignore
      }
    }
    doc.withReferences(references.map(_.unit))
  }

  def parseDocument(): Document = {
    val ast = root.parsed.asInstanceOf[AntlrParsedDocument].ast
    loadReferences(root.references)
    ast.root() match {
      case node: Node =>
        parseWebAPI(node)
        parseMessages(node)
        parseEnums(node)
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

  def parseEnums(node: Node): Unit = {
    collect(node, Seq(TOP_LEVEL_DEF, ENUM_DEF)).zipWithIndex.foreach { case (element: ASTElement, idx: Int) =>
      withNode(element) { node =>
        val shape = GrpcEnumParser(node).parse(shape => {
          shape.name.option() match {
            case None => shape.withName(s"Enum${idx}")
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
