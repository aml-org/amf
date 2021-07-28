package amf.grpc.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.core.client.common.position.Position
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext

case class GrpcServiceEmitter(endPoint: EndPoint, builder: StringDocBuilder, ctx: GrpcEmitterContext) extends GrpcEmitter {

  def emit(): Unit = {
    builder.fixed { f =>
      f += (s"service $name {", servicePos)
      f.obj { o =>
        emitOperations(o)
      }
      f += "}"
    }
  }

  def name: String = endPoint.name.option().getOrElse {
    endPoint.path.value().split("/").mkString("")
  }

  def emitOperations(builder: StringDocBuilder): StringDocBuilder = {
    builder.list { l =>
      endPoint.operations.foreach { op =>
        GrpcRPCEmitter(op, l, ctx).emit()
      }
      emitOptions(endPoint, l, ctx)
    }
  }

  def servicePos: Position = pos(endPoint.annotations)
}
