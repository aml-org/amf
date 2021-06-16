package amf.plugins.document.apicontract.parser.spec.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.core.client.scala.parse.document.ParserContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import org.mulesoft.antlrast.ast.ASTElement

class GrpcServiceParser(ast: ASTElement)(implicit val ctx: ParserContext) extends AntlrASTParserHelper  {
  val endpoint: EndPoint = EndPoint(toAnnotations(ast))

  def parse(idx: Int): EndPoint = {
    parseName(idx)
    endpoint
  }

    def parseName(idx: Int): Unit = {
      path(ast, Seq(SERVICE_NAME, IDENTIFIER)) map { node =>
        withOptTerminal(node) {
          case Some(serviceName) =>
            endpoint.withId(ctx.rootContextDocument + s"/services/${serviceName}").withName(serviceName.value, toAnnotations(node))
          case None              =>
            endpoint.withId(ctx.rootContextDocument + s"/services/${idx}")
            astError(endpoint.id, "missing Protobuf3 service name", endpoint.annotations)
        }
      }
    }
}

object GrpcServiceParser {
  def apply(ast: ASTElement)(implicit ctx: ParserContext) = new GrpcServiceParser(ast)
}