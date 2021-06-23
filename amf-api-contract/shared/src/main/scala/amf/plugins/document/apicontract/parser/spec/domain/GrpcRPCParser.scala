package amf.plugins.document.apicontract.parser.spec.domain

import amf.apicontract.client.scala.model.domain.Operation
import amf.core.internal.remote.Mimes
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GrpcRPCParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends AntlrASTParserHelper  {
  val rpc: Node = find(ast, RPC).headOption.getOrElse(ast).asInstanceOf[Node]

  def parse(adopt: Operation => Unit): Operation = {
    parseServiceMessages(adopt)
  }

  def parseServiceMessages(adopt: Operation => Unit): Operation = {
    val operationName = parseName()
    val messages: Seq[String] = collect(rpc, Seq(MESSAGE_TYPE, MESSAGE_NAME)).map { case n: Node => n.source }
    val request = messages.head
    val response = messages.last
    parseStreamingMetadata() match {
      case (false, false) => buildOperation(operationName,"post", request, response, adopt)
      case (true, false)  => buildOperation(operationName,"publish", request, response, adopt)
      case (false, true)  => buildOperation(operationName,"subscribe", request, response, adopt)
      case (true, true)   => buildOperation(operationName,"pubsub", request, response, adopt)
    }
  }

  def buildOperation(operationName: String, operationType: String, request: String, response: String, adopt: Operation => Unit): Operation = {
    val operation = Operation(toAnnotations(rpc)).withName(operationName).withOperationId(operationName).withMethod(operationType)
    adopt(operation)
    val requestBody = parseObjectRange(rpc, request)
    operation.withRequest()
      .withPayload(Some(Mimes.`APPLICATION/GRPC`))
      .withSchema(requestBody)
    val responseBody = parseObjectRange(rpc, response)
    operation.withResponse("")
      .withPayload(Some(Mimes.`APPLICATION/PROTOBUF`))
      .withSchema(responseBody)
    operation
  }

  def parseStreamingMetadata(): (Boolean, Boolean) = {
    var foundRequest = false
    var streamRequest = false
    var streamResponse = false
    rpc.children.foreach {
      case n: Node if n.name == MESSAGE_TYPE && !foundRequest => foundRequest = true
      case t: Terminal if t.value == "stream" && !foundRequest => streamRequest = true
      case t: Terminal if t.value == "stream" && foundRequest  => streamResponse = true
      case _  => //ignore
    }
    (streamRequest, streamResponse)
  }

  def parseName(): String = {
    path(rpc, Seq(RPC_NAME, IDENTIFIER)) match {
      case Some(node) =>
        withOptTerminal(node) {
          case Some(name) =>
            name.value
          case None       =>
            astError("", "Missing mandatory proto3 rpcName", toAnnotations(node))
            "AnonymousOperation"
        }
      case _         =>
        astError("", "Missing mandatory proto3 rpcName", toAnnotations(ast))
        "AnonymousOperation"
    }
  }

}
