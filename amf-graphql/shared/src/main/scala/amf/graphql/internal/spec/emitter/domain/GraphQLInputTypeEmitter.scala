package amf.graphql.internal.spec.emitter.domain

import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.NodeShape
import amf.graphql.internal.spec.emitter.helpers.StringBuilder

case class GraphQLInputTypeEmitter(
    node: NodeShape,
    extensionPrefix: String,
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder
) extends GraphQLTypeWithFieldsEmitter(node, ctx, b) {

  override def buildTypeString(): String = {
    val name       = node.name.value()
    val directives = GraphQLDirectiveApplicationsRenderer(node)

    StringBuilder(extensionPrefix, "input", name, directives)
  }

  override def emitFields(f: StringDocBuilder): Unit = {
    f.obj { o =>
      o.list { l =>
        emitFieldsWithNoArguments(l)
      }
    }
  }
}
