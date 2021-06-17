package amf.plugins.document.apicontract.parser.spec.domain

import amf.apicontract.client.scala.model.domain.Operation
import amf.core.internal.remote.Mimes
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GrpcRPCParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends AntlrASTParserHelper  {
  val rpc: Node = find(ast, RPC).headOption.getOrElse(ast).asInstanceOf[Node]

  def parse(adopt: Operation => Unit): Seq[Operation] = {
    parseServiceMessages(adopt)
  }

  def parseServiceMessages(adopt: Operation => Unit): Seq[Operation] = {
    val operationName = parseName()
    val messages: Seq[String] = collect(rpc, Seq(MESSAGE_TYPE, MESSAGE_NAME)).map { case n: Node => n.source }
    val request = messages.head
    val response = messages.last
    parseStreamingMetadata() match {
      case (false, false) => Seq(buildSyncOperation(operationName, Some(request), Some(response), adopt))
      case (true, false)  => Seq(buildSyncOperation(operationName,None, Some(response), adopt), buildAsyncOperation(operationName, "publish", request, adopt))
      case (false, true)  => Seq(buildSyncOperation(operationName,Some(request), None, adopt), buildAsyncOperation(operationName, "subscribe", response, adopt))
      case (true, true)   => Seq(buildAsyncOperation(operationName, "publish", request, adopt), buildAsyncOperation(operationName, "subscribe", response, adopt))
    }
  }

  def buildSyncOperation(operationName: String, request: Option[String], response: Option[String], adopt: Operation => Unit): Operation = {
    val operation = Operation(toAnnotations(rpc)).withName(operationName).withOperationId(operationName).withMethod("post")
    adopt(operation)
    request match {
      case Some(reference) =>
        val requestBody = parseObjectRange(rpc, reference)
        operation.withRequest()
          .withPayload(Some(Mimes.`APPLICATION/PROTOBUF`))
          .withSchema(requestBody)
      case _               => // ignore
    }
    response match {
      case Some(reference) =>
        val responseBody = parseObjectRange(rpc, reference)
        operation.withResponse("")
          .withPayload(Some(Mimes.`APPLICATION/PROTOBUF`))
          .withSchema(responseBody)
      case _               => // ignore
    }
    operation
  }

  def buildAsyncOperation(operationName: String, operationType: String, reference: String, adopt: Operation => Unit): Operation = {
    val operation = Operation(toAnnotations(rpc)).withName(operationName).withOperationId(operationName + operationType)
    adopt(operation)
    val body = parseObjectRange(rpc, reference)
    operationType match {
      case "publish" =>
        operation.withMethod("publish").withRequest().withPayload(Some(Mimes.`APPLICATION/PROTOBUF`)).withSchema(body)
      case "subscribe" =>
        operation.withMethod("subscribe").withResponse("").withPayload(Some(Mimes.`APPLICATION/PROTOBUF`)).withSchema(body)
    }
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
