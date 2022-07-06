package amf.grpc.internal.spec.emitter.domain

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.grpc.internal.spec.emitter.domain
import amf.shapes.client.scala.model.domain.ArrayShape

class GrpcFieldEmitter(property: PropertyShape, builder: StringDocBuilder, ctx: GrpcEmitterContext)
    extends GrpcEmitter {

  def emit(): Unit = {
    if (mustEmitOptions(property)) {
      emitWithOptions()
    } else {
      builder += (s"$repeated${fieldRange(property.range)} $fieldName = $fieldNumber;", position)
    }
  }

  private def emitWithOptions(): Unit = {
    if (property.customDomainProperties.length == 1) {
      val inlinedOption = builder.inilined { b =>
        domain.GrpcOptionsEmitter(property.customDomainProperties.head, b, ctx).emitFieldExtension()
      }
      builder += (s"$repeated${fieldRange(property.range)} $fieldName = $fieldNumber [$inlinedOption];", position)
    } else {
      builder.fixed { f =>
        f += (s"$repeated${fieldRange(property.range)} $fieldName = $fieldNumber [", position)
        f.obj { o =>
          o.listWithDelimiter(",\n") { l =>
            property.customDomainProperties.foreach { cdp =>
              domain.GrpcOptionsEmitter(cdp, l, ctx).emitFieldExtension()
            }
          }
        }
        f += "];"
      }
    }
  }

  def position: Position = pos(property.range.annotations)

  def fieldName: String = property.displayName.option().getOrElse(property.name.value())

  def fieldNumber: Int = property.serializationOrder.option().getOrElse(0)

  def repeated: String = if (property.range.isInstanceOf[ArrayShape]) { "repeated " }
  else { "" }

}

object GrpcFieldEmitter {
  def apply(property: PropertyShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) =
    new GrpcFieldEmitter(property, builder, ctx)
}
