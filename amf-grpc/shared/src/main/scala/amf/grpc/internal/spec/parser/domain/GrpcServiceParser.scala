package amf.grpc.internal.spec.parser.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.Node

case class GrpcServiceParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val endpoint: EndPoint = EndPoint(toAnnotations(ast))

  def parse(setterFn: EndPoint => Unit): EndPoint = {
    parseName()
    setterFn(endpoint)
    parseRPCs()
    parseOptions()
    endpoint
  }

  def parseOptions(): Unit = {
    collectOptions(
      ast,
      Seq(SERVICE_ELEMENT, OPTION_STATEMENT),
      ex => endpoint.withCustomDomainProperty(ex)
    )
  }

  private def parseRPCs(): Unit = {
    collect(ast, Seq(SERVICE_ELEMENT, RPC)) foreach { case node: Node =>
      GrpcRPCParser(node).parse(op => endpoint.withOperations(endpoint.operations :+ op))
    }
  }

  def parseName(): Unit = {
    path(ast, Seq(SERVICE_NAME, IDENTIFIER)) foreach { node =>
      withOptTerminal(node) {
        case Some(serviceName) =>
          endpoint.withName(serviceName.value)
        case None =>
          astError("missing Protobuf3 service name", endpoint.annotations)
      }
    }
  }
}
