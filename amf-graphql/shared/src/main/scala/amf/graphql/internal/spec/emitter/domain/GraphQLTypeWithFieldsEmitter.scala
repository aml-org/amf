package amf.graphql.internal.spec.emitter.domain

import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.helpers.LineEmitter
import amf.shapes.client.scala.model.domain.NodeShape

abstract class GraphQLTypeWithFieldsEmitter(
    node: NodeShape,
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder
) {
  def emit(): Unit = {
    val typeString = buildTypeString()

    b.fixed { f =>
      LineEmitter(f, typeString, "{").emit()
      emitFields(f)
      LineEmitter(f, "}").emit()
    }
  }

  protected def buildTypeString(): String

  protected def emitFields(f: StringDocBuilder): Unit

  protected def emitFieldsWithArguments(l: StringDocBuilder): Unit = {
    node.operations.foreach { op =>
      GraphQLOperationFieldEmitter(op, ctx, l).emit()
    }
  }

  protected def emitFieldsWithNoArguments(l: StringDocBuilder): Unit = {
    node.properties.foreach { prop =>
      GraphQLPropertyFieldEmitter(prop, ctx, l).emit()
    }
  }
}
