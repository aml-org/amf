package amf.graphql.internal.spec.emitter.domain

import amf.core.client.scala.model.domain.ScalarNode
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, UnionShape}

case class GraphQLTypeEmitter(shape: AnyShape, ctx: GraphQLEmitterContext, b: StringDocBuilder) {

  def emit(): Unit = {
    shape match {
      case enum: ScalarShape if enum.values.nonEmpty =>
        emitEnum(enum, b)
      case node: NodeShape =>
        emitObject(node, b)
      case union: UnionShape =>
        emitUnion(union, b)
    }
  }

  def renderInheritance(nodeShape: NodeShape): String = {
    nodeShape.effectiveInherits.headOption map { iface =>
      s"implements ${iface.name.value()} "
    } getOrElse ""
  }

  def checkObjectType(name: String, shape: NodeShape): String = {
    if (ctx.inputTypeNames.contains(name)) {
      "input"
    } else if (shape.isAbstract.option().getOrElse(false)) {
      "interface"
    } else {
      "type"
    }
  }

  def emitObject(node: NodeShape, b: StringDocBuilder): Unit = {
    b.fixed { f =>
      val maybeInheritance = renderInheritance(node)
      val name = shape.name.value()
      val effectiveType = checkObjectType(name, node)
      f.+=(s"$effectiveType $name $maybeInheritance{", pos(node.annotations))
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

  def emitUnion(shape: UnionShape, b: StringDocBuilder): Unit = {
    b.fixed { f =>
      val members = shape.anyOf.map(_.name.value()).mkString(" | ")
      f.+=(s"union ${shape.name.value()} = $members", pos(shape.annotations))
    }
  }

  def emitEnum(shape: ScalarShape, b: StringDocBuilder): Unit = {
    b.fixed { f =>
      val name = shape.name.value()
      val members = shape.values.collect { case s: ScalarNode => s }
      f.+=(s"enum $name {", pos(shape.annotations))
      f.obj { o =>
        members.foreach { value =>
          o.+=(s"${value.value.value()}", pos(value.annotations))
        }
      }
      f.+=("}")
    }
  }
}
