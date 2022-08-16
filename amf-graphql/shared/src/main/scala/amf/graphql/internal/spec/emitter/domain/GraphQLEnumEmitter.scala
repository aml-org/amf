package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.core.client.scala.model.domain.ScalarNode

case class GraphQLEnumEmitter(enum: ScalarShape, extensionPrefix: String, ctx: GraphQLEmitterContext, b: StringDocBuilder) {
  def emit(): Unit = {
    val name       = enum.name.value()
    b += (s"${extensionPrefix}enum $name {", pos(enum.annotations))
    emitEnumValues()
    b += "}"
  }
  private def emitEnumValues() = {
    val enumValues = enum.values.collect { case s: ScalarNode => s }
    b.obj { o =>
      enumValues.foreach { enumValue =>
        o += (s"${enumValue.value.value()}", pos(enumValue.annotations))
      }
    }
  }
}
