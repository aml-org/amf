package amf.grpc.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.Operation
import org.mulesoft.common.client.lexical.Position
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext

case class GrpcRPCEmitter(operation: Operation, builder: StringDocBuilder, ctx: GrpcEmitterContext)
    extends GrpcEmitter {

  // Constants for unknown types and streaming methods
  private val UnknownMessage           = "UnknownMessage"
  private val RequestStreamingMethods  = Set("publish", "pubsub")
  private val ResponseStreamingMethods = Set("subscribe", "pubsub")

  def emit(): Unit = {
    val rpcSignature = buildRpcSignature
    if (mustEmitOptions(operation)) {
      emitRpcWithOptions(rpcSignature)
    } else {
      builder += (s"$rpcSignature {}", operationPos)
    }
  }

  /** Builds the complete RPC method signature */
  private def buildRpcSignature: String = {
    val requestType  = s"$streamRequest$request"
    val responseType = s"$streamResponse$response"
    s"rpc $name($requestType) returns ($responseType)"
  }

  /** Emits RPC with options block */
  private def emitRpcWithOptions(signature: String): Unit = {
    builder.fixed { f =>
      f += (s"$signature {", operationPos)
      f.obj { o =>
        o.list { l =>
          emitOptions(operation, l, ctx)
        }
      }
      f += "}"
    }
  }

  private def operationPos: Position = pos(operation.annotations)

  /** Resolves the RPC method name from operation metadata */
  private def name: String = operation.operationId
    .option()
    .orElse(operation.name.option())
    .getOrElse(s"operation${operation.method.value()}")

  private def request: String = operation.request.payloads.headOption
    .map(schema => fieldRange(schema.schema))
    .getOrElse(UnknownMessage)

  private def response: String = operation.responses.head.payloads.headOption
    .map(schema => fieldRange(schema.schema))
    .getOrElse(UnknownMessage)

  /** Determines if should emit 'stream' for request */
  private def streamRequest: String =
    if (operation.method.option().exists(RequestStreamingMethods.contains)) "stream " else ""

  /** Determines if should emit 'stream' for response */
  private def streamResponse: String =
    if (operation.method.option().exists(ResponseStreamingMethods.contains)) "stream " else ""
}
