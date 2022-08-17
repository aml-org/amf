package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
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
    val directives       = GraphQLDirectiveApplicationsRenderer(union)
    val unionMemberTypes = "= " ++ members.mkString(" | ")
    LineEmitter(b, extensionPrefix, "union", name, directives, unionMemberTypes).emit()
  }
  private def members = union.anyOf.map(_.name.value())
}
