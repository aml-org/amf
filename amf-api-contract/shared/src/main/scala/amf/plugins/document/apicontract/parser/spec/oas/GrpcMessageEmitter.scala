package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.common.position.Position
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.shapes.client.scala.model.domain.{NodeShape, UnionShape}

case class GrpcMessageEmitter(shape: NodeShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) extends GrpcEmitter {
  def emit(): Unit = {
    builder.fixed { f =>
      f += (s"message ${messageName} {", messageNamePos)
      f.obj { o =>
        o.list {l =>
          emitProperties(l)
          emitOptions(shape, l, ctx)
          ctx.nestedMessages(shape).foreach { nested =>
            GrpcMessageEmitter(nested, l, ctx).emit()
          }
          ctx.nestedEnums(shape).foreach { nested =>
            GrpcEnumEmitter(nested, l, ctx).emit()
          }
          emitOneOf(l)
        }
      }
      f += ("}")
    }

  }

  def messageName: String = shape.displayName.option().getOrElse("AnonymousMessage")
  def messageNamePos: Position = pos(shape.displayName.annotations())

  def emitOneOf(builder: StringDocBuilder): Unit = {
    shape.and.foreach { case union: UnionShape =>
      GrpcOneOfEmitter(union, builder, ctx).emit()
    }
  }

  def emitProperties(builder: StringDocBuilder): Unit = shape.properties.foreach { p =>
    GrpcFieldEmitter(p, builder, ctx).emit()
  }
}

object GrpcMessageEmitter {
  def apply(shape: NodeShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) = new GrpcMessageEmitter(shape, builder, ctx)
}
