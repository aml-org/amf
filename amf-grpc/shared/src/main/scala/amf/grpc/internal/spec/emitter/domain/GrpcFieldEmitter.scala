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
      emitFieldWithOptions()
    } else {
      builder += (s"${buildFieldSignature()};", position)
    }
  }

  private def emitFieldWithOptions(): Unit = {
    if (property.customDomainProperties.length == 1) {
      emitFieldWithSingleOption()
    } else {
      emitFieldWithMultipleOptions()
    }
  }

  private def emitFieldWithSingleOption(): Unit = {
    val cleanOption = buildSingleOptionContent(property.customDomainProperties.head, builder, ctx)
    builder += (s"${buildFieldSignature()} [$cleanOption];", position)
  }

  private def emitFieldWithMultipleOptions(): Unit = {
    builder.fixed { f =>
      f += (s"${buildFieldSignature()} [", position)
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

  private def buildFieldSignature(): String = {
    s"$repeated${fieldRange(property.range)} $fieldName = $fieldNumber"
  }

  private def position: Position = pos(property.range.annotations)

  private def fieldName: String = property.displayName.option().getOrElse(property.name.value())

  private def fieldNumber: Int = property.serializationOrder.option().getOrElse(0)

  private def repeated: String = if (property.range.isInstanceOf[ArrayShape]) "repeated " else ""

}

object GrpcFieldEmitter {
  def apply(property: PropertyShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) =
    new GrpcFieldEmitter(property, builder, ctx)
}
