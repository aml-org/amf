package amf.grpc.internal.spec.parser.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.internal.metamodel.domain.EndPointModel
import amf.core.internal.parser.domain.Annotations
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.Node

case class GrpcServiceParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val ann: Annotations = toAnnotations(ast)
  val endpoint: EndPoint = EndPoint(ann)

  def parse(setterFn: EndPoint => Unit = _ => ()): EndPoint = {
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
      ex => {
        val extensions = endpoint.customDomainProperties :+ ex
        endpoint set (extensions, ann) as EndPointModel.CustomDomainProperties
      }
    )
  }

  private def parseRPCs(): Unit = {
    collect(ast, Seq(SERVICE_ELEMENT, RPC)) foreach { case node: Node =>
      GrpcRPCParser(node).parse { op =>
        val newOps = endpoint.operations :+ op
        endpoint set (newOps, ann) as EndPointModel.Operations
      }
    }
  }

  def parseName(): Unit = {
    path(ast, Seq(SERVICE_NAME, IDENTIFIER)) foreach { node =>
      withOptTerminal(node) {
        case Some(serviceName) =>
          endpoint.withName(serviceName.value, toAnnotations(ast))
        case None =>
          astError("missing Protobuf3 service name", endpoint.annotations)
      }
    }
  }
}
