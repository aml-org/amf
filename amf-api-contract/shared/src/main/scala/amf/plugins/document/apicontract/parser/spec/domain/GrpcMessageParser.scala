package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.client.scala.parse.document.ParserContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.antlrast.ast.Node

class GrpcMessageParser (ast: Node)(implicit val ctx: ParserContext) extends AntlrASTParserHelper {
  val nodeShape = NodeShape(toAnnotations(ast))

  def parse(): NodeShape = {
    path(ast, Seq(MESSAGE_NAME, IDENTIFIER)).foreach { node =>
      withOptTerminal(node) {
        case Some(shapeName) =>
          nodeShape.withName(shapeName.value)
        case None              =>
          astError(nodeShape.id, "missing Protobuf3 message name", nodeShape.annotations)
      }
    }
    nodeShape
  }
}

object GrpcMessageParser {
  def apply(ast: Node)(implicit ctx: ParserContext) = new GrpcMessageParser(ast)
}