package amf.grpc.internal.spec.parser.domain

import amf.apicontract.client.scala.model.domain.Operation
import amf.core.internal.remote.Mimes
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GrpcRPCParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {

  def parse(adopt: Operation => Unit): Operation = {
    parseServiceMessages(adopt)
  }

  def parseServiceMessages(adopt: Operation => Unit): Operation = {
    val operationName         = parseName()
    val messages: Seq[String] = collect(ast, Seq(MESSAGE_TYPE, MESSAGE_NAME)).map { case n: Node => n.source }
    val request               = messages.head
    val response              = messages.last
    val operation = parseStreamingMetadata() match {
      case (false, false) => buildOperation(operationName, "post", request, response, adopt)
      case (true, false)  => buildOperation(operationName, "publish", request, response, adopt)
      case (false, true)  => buildOperation(operationName, "subscribe", request, response, adopt)
      case (true, true)   => buildOperation(operationName, "pubsub", request, response, adopt)
    }
    parseOptions(ast, operation)
    operation
  }

  def parseOptions(ast: Node, operation: Operation): Unit = {
    collectOptions(ast, Seq(OPTION_STATEMENT), { extension =>
      extension.adopted(operation.id)
      operation.withCustomDomainProperty(extension)
    })
  }

  def buildOperation(operationName: String,
                     operationType: String,
                     request: String,
                     response: String,
                     adopt: Operation => Unit): Operation = {
    val operation =
      Operation(toAnnotations(ast)).withName(operationName).withOperationId(operationName).withMethod(operationType)
    adopt(operation)
    val requestBody = parseObjectRange(ast, request)
    operation
      .withRequest()
      .withPayload(Some(Mimes.`application/grpc`))
      .withSchema(requestBody)
    val responseBody = parseObjectRange(ast, response)
    operation
      .withResponse("")
      .withPayload(Some(Mimes.`application/protobuf`))
      .withSchema(responseBody)
    operation
  }

  def parseStreamingMetadata(): (Boolean, Boolean) = {
    var foundRequest   = false
    var streamRequest  = false
    var streamResponse = false
    ast.children.foreach {
      case n: Node if n.name == MESSAGE_TYPE && !foundRequest  => foundRequest = true
      case t: Terminal if t.value == "stream" && !foundRequest => streamRequest = true
      case t: Terminal if t.value == "stream" && foundRequest  => streamResponse = true
      case _                                                   => //ignore
    }
    (streamRequest, streamResponse)
  }

  def parseName(): String = {
    path(ast, Seq(RPC_NAME, IDENTIFIER)) match {
      case Some(node) =>
        withOptTerminal(node) {
          case Some(name) =>
            name.value
          case None =>
            astError("", "Missing mandatory proto3 rpcName", toAnnotations(node))
            "AnonymousOperation"
        }
      case _ =>
        astError("", "Missing mandatory proto3 rpcName", toAnnotations(ast))
        "AnonymousOperation"
    }
  }

}
