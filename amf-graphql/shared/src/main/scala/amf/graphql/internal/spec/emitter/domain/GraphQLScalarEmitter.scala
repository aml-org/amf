package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.helpers.LineEmitter
import amf.shapes.client.scala.model.domain.ScalarShape

case class GraphQLScalarEmitter(scalar: ScalarShape, extensionPrefix: String, ctx: GraphQLEmitterContext, b: StringDocBuilder) {
  def emit(): Unit = {
    val name = scalar.name.value()
    LineEmitter(b, extensionPrefix, "scalar", name).emit()
  }
}