package amf.plugins.document.apicontract.parser.spec.oas

import amf.client.remod.amfcore.plugins.render.StringDocBuilder
import amf.core.emitter.BaseEmitters._
import amf.core.parser.Position
import amf.plugins.domain.apicontract.models.EndPoint

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


