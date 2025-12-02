package amf.grpc.internal.spec.emitter.domain

import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.common.client.lexical.Position

case class GrpcMessageEmitter(shape: NodeShape, builder: StringDocBuilder, ctx: GrpcEmitterContext)
    extends GrpcEmitter {
  def emit(): Unit = {
    builder.fixed { f =>
      f += (s"message $messageName {", messageNamePos)
      f.obj { o =>
        o.list { l =>
          emitProperties(l)
          emitOptions(shape, l, ctx)
          ctx.nestedMessages(shape).foreach { nested =>
            GrpcMessageEmitter(nested, l, ctx).emit()
          }
          ctx.nestedEnums(shape).foreach { nested =>
            GrpcEnumEmitter(nested, l, ctx).emit()
          }
          GrpcReservedEmitter(shape, l, ctx).emit()
        }
      }
      f += "}"
    }

  }

  private def messageName: String      = shape.displayName.option().getOrElse("AnonymousMessage")
  private def messageNamePos: Position = pos(shape.displayName.annotations())

  private def emitProperties(builder: StringDocBuilder): Unit = shape.properties.foreach {
    case oneOf if oneOf.range.isXOne => GrpcOneOfEmitter(oneOf, builder, ctx).emit()
    case p                           => GrpcFieldEmitter(p, builder, ctx).emit()
  }
}

object GrpcMessageEmitter {
  def apply(shape: NodeShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) =
    new GrpcMessageEmitter(shape, builder, ctx)
}
