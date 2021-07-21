package amf.plugins.document.apicontract.parser.spec.oas

import amf.apicontract.client.scala.model.domain.Operation
import amf.core.client.common.position.Position
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos

case class GrpcRPCEmitter(operation: Operation, builder: StringDocBuilder, ctx: GrpcEmitterContext) extends GrpcEmitter {

  def emit(): Unit = {
    if (mustEmitOptions(operation)) {
      builder.fixed { f =>
        f += (s"rpc $name($streamRequest$request) returns ($streamResponse$response) {", operationPos)
        f.obj { o =>
          o.list { l =>
            emitOptions(operation, l, ctx)
          }
        }
        f += (s"}")
      }
    } else {
      builder += (s"rpc $name($streamRequest$request) returns ($streamResponse$response) {}", operationPos)
    }

  }

  def operationPos: Position = pos(operation.annotations)

  def name: String = operation.operationId.option()
    .orElse(operation.name.option())
    .getOrElse(s"operation${operation.method.value()}")

  def request: String = operation.request.payloads.headOption.map { schema =>
    fieldRange(schema.schema)
  } getOrElse("UnknownMessage")

  def response: String = operation.responses.head.payloads.headOption.map { schema =>
    fieldRange(schema.schema)
  } getOrElse("UnknownMessage")

  def streamRequest: String = operation.method.option().getOrElse("") match  {
    case "publish" => "stream "
    case "pubsub"  => "stream "
    case _         => ""
  }

  def streamResponse: String = operation.method.option().getOrElse("") match  {
    case "subscribe" => "stream "
    case "pubsub"    => "stream "
    case _           => ""
  }
}
