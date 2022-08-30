package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
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
    val name       = enum.name.value()
    val directives = GraphQLDirectiveApplicationsRenderer(enum)
    LineEmitter(b, extensionPrefix, "enum", name, directives, "{").emit()
    emitEnumValues()
    LineEmitter(b).closeBlock()
  }
  private def emitEnumValues() = {
    val enumValues = enum.values.collect { case s: ScalarNode => s }
    b.obj { o =>
      enumValues.foreach { enumValue =>
        val name       = enumValue.value.value()
        val directives = GraphQLDirectiveApplicationsRenderer(enumValue)
        LineEmitter(o, name, directives).emit()
      }
    }
  }
}
