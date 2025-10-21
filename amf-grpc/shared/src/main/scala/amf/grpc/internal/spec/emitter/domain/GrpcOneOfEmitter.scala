package amf.grpc.internal.spec.emitter.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.common.client.lexical.Position

class GrpcOneOfEmitter(oneOf: PropertyShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) {
  def emit(): Unit = {
    builder.fixed { f =>
      f += (s"oneof $name {", unionPos)
      f.obj { o =>
        emitFields(o)
      }
      f += ("}")
    }
  }

  private def unionPos: Position = pos(oneOf.annotations)
  private def name: String = {
    oneOf.name.option().getOrElse("AnonymousUnion")
  }
  private def emitFields(builder: StringDocBuilder) = {
    builder.list { l =>
      oneOf.range.xone.foreach {
        case member: NodeShape =>
          member.properties.foreach { property =>
            GrpcFieldEmitter(property, l, ctx).emit()
          }
      }
    }
  }
}

object GrpcOneOfEmitter {
  def apply(oneOf: PropertyShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) =
    new GrpcOneOfEmitter(oneOf, builder, ctx)
}
