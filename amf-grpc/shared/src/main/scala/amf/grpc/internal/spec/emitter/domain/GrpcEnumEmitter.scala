package amf.grpc.internal.spec.emitter.domain

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.ScalarNode
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.grpc.internal.spec.emitter.domain
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}

case class GrpcEnumEmitter(shape: ScalarShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) extends GrpcEmitter {

  def emit(): Unit = {
    builder.fixed { l =>
      l += (s"enum $enumName {", enumPos)
      l.obj { o =>
        emitOptions(shape, o, ctx)
        emitValues(o)
      }
      l += "}"
    }
  }

  private def enumName: String  = shape.displayName.option().getOrElse("AnonymousEnum")
  private def enumPos: Position = pos(shape.displayName.annotations())

  private def emitValues(builder: StringDocBuilder): Unit = {
    GrpcReservedEmitter(shape, builder, ctx).emit()
    Option(shape.serializationSchema) match {
      case Some(serialization: NodeShape) => emitSerializationSchemaValues(serialization, builder)
      case _                              => emitFallbackValues(builder)
    }
  }

  private def emitSerializationSchemaValues(serialization: NodeShape, builder: StringDocBuilder): Unit = {
    serialization.properties.foreach { prop =>
      val name  = prop.name.value()
      val value = prop.serializationOrder.value()
      val optionsSuffix = buildEnumValueOptions(prop, builder)
      builder += (s"$name = $value$optionsSuffix;", pos(prop.annotations))
    }
  }

  private def buildEnumValueOptions(prop: amf.core.client.scala.model.domain.extensions.PropertyShape, builder: StringDocBuilder): String = {
    if (prop.customDomainProperties.isEmpty) {
      ""
    } else if (prop.customDomainProperties.length == 1) {
      val cleanOption = buildSingleOptionContent(prop.customDomainProperties.head, builder, ctx)
      s" [$cleanOption]"
    } else {
      // Multiple options - would need bracket list handling if this case occurs
      ""
    }
  }

  private def emitFallbackValues(builder: StringDocBuilder): Unit = {
    shape.values.zipWithIndex.foreach {
      case (data: ScalarNode, idx) =>
        val value = data.value.value()
        builder += (s"$value = $idx;", pos(data.annotations))
      case _ => // ignore non-scalar nodes
    }
  }
}

object GrpcEnumEmitter {
  def apply(nested: ScalarShape, b: StringDocBuilder, ctx: GrpcEmitterContext) = new GrpcEnumEmitter(nested, b, ctx)
}
