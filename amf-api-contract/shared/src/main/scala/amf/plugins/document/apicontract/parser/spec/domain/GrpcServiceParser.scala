package amf.plugins.document.apicontract.parser.spec.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import org.mulesoft.antlrast.ast.Node

case class GrpcServiceParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends AntlrASTParserHelper  {
  val endpoint: EndPoint = EndPoint(toAnnotations(ast))

  def parse(adopt: EndPoint => Unit): EndPoint = {
    parseName(adopt)
    parseRPCs()
    endpoint
  }

  def parseRPCs(): Unit = {
    collect(ast, Seq(SERVICE_ELEMENT)) foreach { case node: Node =>
      GrpcRPCParser(node).parse({ operation: Operation =>
        operation.adopted(endpoint.id)
        endpoint.withOperations(endpoint.operations ++ Seq(operation))
      })
    }
  }

  def parseName(adopt: EndPoint => Unit): Unit = {
    path(ast, Seq(SERVICE_NAME, IDENTIFIER)) foreach { node =>
      withOptTerminal(node) {
        case Some(serviceName) =>
          endpoint.withName(serviceName.value)
          adopt(endpoint)
        case None              =>
          astError(endpoint.id, "missing Protobuf3 service name", endpoint.annotations)
      }
    }
  }
}