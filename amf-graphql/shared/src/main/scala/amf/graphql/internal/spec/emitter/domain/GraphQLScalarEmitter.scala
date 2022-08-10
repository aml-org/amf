package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.ScalarShape

case class GraphQLScalarEmitter(scalar: ScalarShape, extensionPrefix: String, ctx: GraphQLEmitterContext, b: StringDocBuilder) {
  def emit(): Unit = {
    val name = scalar.name.value()
    b += (s"${extensionPrefix}scalar $name", pos(scalar.annotations))
  }
}