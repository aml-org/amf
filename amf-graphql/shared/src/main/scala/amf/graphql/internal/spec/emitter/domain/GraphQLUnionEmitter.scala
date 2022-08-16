package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.UnionShape

case class GraphQLUnionEmitter(union: UnionShape, extensionPrefix: String, ctx: GraphQLEmitterContext, b: StringDocBuilder){
  def emit():Unit = {
    val name = union.name.value()
    val members = union.anyOf.map(_.name.value()).mkString(" | ")
    b += (s"${extensionPrefix}union $name = $members", pos(union.annotations))
  }
}
