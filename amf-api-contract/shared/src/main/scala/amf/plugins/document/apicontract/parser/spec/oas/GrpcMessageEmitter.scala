package amf.plugins.document.apicontract.parser.spec.oas

import amf.client.remod.amfcore.plugins.render.StringDocBuilder
import amf.plugins.domain.shapes.models.{NodeShape, UnionShape}
import amf.core.emitter.BaseEmitters._
import amf.core.parser.Position

case class GrpcMessageEmitter(shape: NodeShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) {
  def emit(): Unit = {
    builder.fixed { f =>
      f += (s"message ${messageName} {", messageNamePos)
      f.obj { o =>
        o.list {l =>
          emitProperties(l)

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
