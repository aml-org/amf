package amf.grpc.internal.spec.emitter.domain

import amf.core.client.common.position.Position
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.shapes.client.scala.model.domain.{NodeShape, UnionShape}

class GrpcOneOfEmitter(union: UnionShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) {
  def emit(): Unit = {
    builder.fixed { f =>
      f += (s"oneof $name {", unionPos)
      f.obj { o =>
        emitFields(o)
      }
      f += ("}")
    }
  }

  def unionPos: Position = pos(union.annotations)
  def name: String = {
    union.name.option().getOrElse("AnonymousUnion")
  }
  def emitFields(builder: StringDocBuilder) = {
    builder.list { l =>
      union.anyOf.foreach { case member:NodeShape =>
        member.properties.foreach { property =>
          GrpcFieldEmitter(property, l, ctx).emit()
        }
      }
    }
  }
}

object GrpcOneOfEmitter {
  def apply(union: UnionShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) = new GrpcOneOfEmitter(union, builder, ctx)
}
