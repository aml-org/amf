package amf.graphql.internal.spec.emitter.domain

import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}

case class GraphQLTypeEmitter(shape: AnyShape, ctx: GraphQLEmitterContext, b: StringDocBuilder) {

  def emit(): Unit = {
    shape match {
      case node: NodeShape =>
        emitObject(node, b)
    }
  }

  def emitObject(node: NodeShape, b: StringDocBuilder) = {
    b.fixed { f =>
      f.+=(s"type ${shape.name.value()} {")
      f.obj { o =>
        o.list { l =>
          node.properties.foreach { prop =>
            GraphQLPropertyFieldEmitter(prop, ctx, l).emit()
          }
          node.operations.foreach { op =>
            GraphQLOperationFieldEmitter(op, ctx, l).emit()
          }
        }
      }
      f.+=("}")
    }
  }
}
