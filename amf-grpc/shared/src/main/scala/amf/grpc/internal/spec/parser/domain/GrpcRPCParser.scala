package amf.grpc.internal.spec.parser.domain

import amf.apicontract.client.scala.model.domain.{Operation, Payload, Request, Response}
import amf.apicontract.internal.metamodel.domain.{OperationModel, PayloadModel, RequestModel, ResponseModel}
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Mimes
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GrpcRPCParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {

  private val STREAM_KEYWORD = "stream"

  private object OperationType {
    val POST      = "post"
    val PUBLISH   = "publish"
    val SUBSCRIBE = "subscribe"
    val PUBSUB    = "pubsub"
  }

  def parse(setterFn: Operation => Unit): Operation = {
    val operationName               = parseName()
    val (requestName, responseName) = parseMessageTypes()
    val operationType               = determineOperationType()
    val operation                   = buildOperation(operationName, operationType, requestName, responseName)
    parseOptions(operation)
    setterFn(operation)
    operation
  }

  private def parseOptions(operation: Operation): Unit = {
    collectOptions(
        ast,
        Seq(OPTION_STATEMENT),
        ex => {
          val extensions = operation.customDomainProperties :+ ex
          operation set (extensions, toAnnotations(ast)) as OperationModel.CustomDomainProperties
        }
    )
  }

  private def parseMessageTypes(): (OperationMessage, OperationMessage) = {
    val messages = collect(ast, Seq(MESSAGE_TYPE)).map { case n: Node => OperationMessage(n.source, toAnnotations(n)) }
    if (messages.size != 2) {
      astError("Invalid number of messages", toAnnotations(ast))

      (OperationMessage("AnonymousOperation", Annotations.synthesized()),
       OperationMessage("AnonymousOperation", Annotations.synthesized()))
    }
    else {
      (messages.head, messages.last)
    }
  }

  private def determineOperationType(): String = {
    val (streamRequest, streamResponse) = parseStreamingMetadata()
    (streamRequest, streamResponse) match {
      case (false, false) => OperationType.POST
      case (true, false)  => OperationType.PUBLISH
      case (false, true)  => OperationType.SUBSCRIBE
      case (true, true)   => OperationType.PUBSUB
    }
  }

  private def buildOperation(
      operationName: String,
      operationType: String,
      requestMessage: OperationMessage,
      responseMessage: OperationMessage
  ): Operation = {
    val ann       = toAnnotations(ast)
    val operation = Operation(ann).withName(operationName, ann)
    operation set (operationName, ann) as OperationModel.OperationId
    operation set (operationType, ann) as OperationModel.Method

    val request = createRequest(requestMessage.name, requestMessage.lexical)
    operation set AmfArray(Seq(request), requestMessage.lexical) as OperationModel.Request

    val response = createResponse(responseMessage.name, responseMessage.lexical)
    operation set AmfArray(Seq(response), responseMessage.lexical) as OperationModel.Responses

    operation
  }

  private def createRequest(name: String, ann: Annotations): Request = {
    val request = Request(ann)
    val payload = Payload(ann)
    payload set (Mimes.`application/grpc`, ann) as PayloadModel.MediaType
    val schema = parseObjectRange(ast, name)
    payload set schema as PayloadModel.Schema
    val payloads = request.payloads :+ payload
    request set (payloads, ann) as RequestModel.Payloads
    request
  }

  private def createResponse(name: String, ann: Annotations): Response = {
    val response = Response(ann)
    val payload  = Payload(ann)
    payload set (Mimes.`application/protobuf`, ann) as PayloadModel.MediaType
    val schema = parseObjectRange(ast, name)
    payload set schema as PayloadModel.Schema
    val payloads = response.payloads :+ payload
    response set (payloads, ann) as ResponseModel.Payloads
    response
  }

  private def parseStreamingMetadata(): (Boolean, Boolean) = {
    var foundRequest   = false
    var streamRequest  = false
    var streamResponse = false
    ast.children.foreach {
      case n: Node if n.name == MESSAGE_TYPE && !foundRequest  => foundRequest = true
      case t: Terminal if t.value == "stream" && !foundRequest => streamRequest = true
      case t: Terminal if t.value == "stream" && foundRequest  => streamResponse = true
      case _                                                   => // ignore
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
            astError("Missing mandatory proto3 rpcName", toAnnotations(node))
            "AnonymousOperation"
        }
      case _ =>
        astError("Missing mandatory proto3 rpcName", toAnnotations(ast))
        "AnonymousOperation"
    }
  }

  private case class OperationMessage(name: String, lexical: Annotations)

}
