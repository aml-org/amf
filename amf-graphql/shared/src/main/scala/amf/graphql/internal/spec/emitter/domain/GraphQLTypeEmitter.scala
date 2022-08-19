package amf.graphql.internal.spec.emitter.domain

import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, UnionShape}

case class GraphQLTypeEmitter(shape: AnyShape, ctx: GraphQLEmitterContext, b: StringDocBuilder) {
  def emit(): Unit = {
    val extensionPrefix = if (isExtension) "extend" else ""
    emitDescription()
    b.fixed { f =>
      shape match {
        case scalar: ScalarShape if hasEnum(scalar) => GraphQLEnumEmitter(scalar, extensionPrefix, ctx, f).emit()
        case scalar: ScalarShape                    => GraphQLScalarEmitter(scalar, extensionPrefix, ctx, f).emit()
        case node: NodeShape                        => GraphQLObjectEmitter(node, extensionPrefix, ctx, f).emit()
        case union: UnionShape                      => GraphQLUnionEmitter(union, extensionPrefix, ctx, f).emit()
        case _                                      => //
      }
    }
  }

  private def isExtension: Boolean = shape.isExtension.value()
  private def emitDescription(): Unit = {
    if(!isExtension){
      val description = shape.description.option()
      GraphQLDescriptionEmitter(description, ctx, b, Some(pos(shape.annotations))).emit()
    }
  }
  private def hasEnum(scalar: ScalarShape) = scalar.values.nonEmpty
}
