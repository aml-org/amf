package amf.plugins.document.apicontract.parser.spec.oas

import amf.client.remod.amfcore.plugins.render.StringDocBuilder
import amf.core.emitter.BaseEmitters._
import amf.core.model.domain.ScalarNode
import amf.core.parser.Position
import amf.plugins.domain.shapes.models.{NodeShape, ScalarShape}

case class GrpcEnumEmitter(shape: ScalarShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) {

  def emit(): Unit = {
    builder.fixed { l =>
      l += (s"enum ${enumName} {", enumPos)
      l.obj { o =>
        emitValues(o)
      }
      l += ("}")
    }
  }

  def enumName: String = shape.displayName.option().getOrElse("AnonymousEnum")
  def enumPos: Position = pos(shape.displayName.annotations())

  def emitValues(builder: StringDocBuilder): Unit = {
    Option(shape.serializationSchema) match {
      case Some(serialization: NodeShape) =>
        serialization.properties.foreach { prop =>
          val name = prop.name.value()
          val value = prop.serializationOrder.value()
          builder += (s"$name = $value;", pos(prop.annotations))
        }
      case _                              =>
        shape.values.zipWithIndex.foreach {
          case (data: ScalarNode, idx) =>
            val value = data.value.value()
            builder += (s"$value = $idx;", pos(data.annotations))
          case _                       => //ignore
        }
    }
  }
}

object GrpcEnumEmitter {
  def apply(nested: ScalarShape, b: StringDocBuilder, ctx: GrpcEmitterContext) = new GrpcEnumEmitter(nested, b, ctx)
}