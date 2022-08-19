package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.core.client.scala.model.domain.ScalarNode
import amf.graphql.internal.spec.emitter.helpers.LineEmitter

case class GraphQLEnumEmitter(
    enum: ScalarShape,
    extensionPrefix: String,
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder
) {
  def emit(): Unit = {
    val name = enum.name.value()
    LineEmitter(b, extensionPrefix, "enum", name, "{").emit()
    emitEnumValues()
    LineEmitter(b, "}").emit()
  }
  private def emitEnumValues() = {
    val enumValues = enum.values.collect { case s: ScalarNode => s }
    b.obj { o =>
      enumValues.foreach { enumValue =>
        LineEmitter(o, enumValue.value.value()).emit()
      }
    }
  }
}
