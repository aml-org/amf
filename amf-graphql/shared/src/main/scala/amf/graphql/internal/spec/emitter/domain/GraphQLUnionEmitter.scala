package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.helpers.LineEmitter
import amf.shapes.client.scala.model.domain.UnionShape

case class GraphQLUnionEmitter(
    union: UnionShape,
    extensionPrefix: String,
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder
) {
  def emit(): Unit = {
    val name             = union.name.value()
    val unionMemberTypes = "= " ++ collectMembers.mkString(" | ")
    LineEmitter(b, extensionPrefix, "union", name, unionMemberTypes).emit()
  }
  private def collectMembers = union.anyOf.map(_.name.value())
}
