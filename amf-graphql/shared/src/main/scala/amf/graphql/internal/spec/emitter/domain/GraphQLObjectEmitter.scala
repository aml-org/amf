package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.NodeShape

case class GraphQLObjectEmitter(
    node: NodeShape,
    extensionPrefix: String,
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder
) {
  def emit(): Unit = {
    b.fixed { f =>
      val name                = node.name.value()
      val concreteGraphQLType = checkObjectType(name)
      renderInheritance() match {
        case Some(implementedInterfaces) =>
          f += (s"$extensionPrefix$concreteGraphQLType $name $implementedInterfaces {", pos(node.annotations))
        case _ =>
          f += (s"$extensionPrefix$concreteGraphQLType $name {", pos(node.annotations))
      }
      emitFields(f)
      f += "}"
    }
  }

  private def renderInheritance(): Option[String] = {
    if (implementsInterfaces) {
      val interfacesNames = node.effectiveInherits.map(_.name.value())
      Some(s"implements ${interfacesNames.mkString(" & ")}")
    } else {
      None
    }
  }

  private def checkObjectType(name: String): String = {
    if (ctx.inputTypeNames.contains(name)) {
      "input"
    } else if (isInterface) {
      "interface"
    } else {
      "type"
    }
  }

  private def isInterface = node.isAbstract.option().getOrElse(false)
  private def emitFields(f: StringDocBuilder): Unit = {
    f.obj { o =>
      o.list { l =>
        emitFieldsWithNoArguments(l)
        emitFieldsWithArguments(l)
      }
    }
  }
  private def emitFieldsWithArguments(l: StringDocBuilder): Unit = {
    node.operations.foreach { op =>
      GraphQLOperationFieldEmitter(op, ctx, l).emit()
    }
  }
  private def emitFieldsWithNoArguments(l: StringDocBuilder): Unit = {
    node.properties.foreach { prop =>
      GraphQLPropertyFieldEmitter(prop, ctx, l).emit()
    }
  }

  private def implementsInterfaces: Boolean = node.effectiveInherits.nonEmpty
}
