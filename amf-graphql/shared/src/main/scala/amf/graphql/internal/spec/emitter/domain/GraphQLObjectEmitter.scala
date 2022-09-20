package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.shapes.client.scala.model.domain.NodeShape
import amf.graphql.internal.spec.emitter.helpers.StringBuilder

case class GraphQLObjectEmitter(
    node: NodeShape,
    extensionPrefix: String,
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder
) extends GraphQLTypeWithFieldsEmitter(node, ctx, b) {

  override def buildTypeString(): String = {
    val name                 = node.name.value()
    val concreteGraphQLType  = checkObjectType()
    val implementsInterfaces = renderInheritance()
    val directives           = GraphQLDirectiveApplicationsRenderer(node)

    StringBuilder(extensionPrefix, concreteGraphQLType, name, implementsInterfaces, directives)
  }

  override def emitFields(f: StringDocBuilder): Unit = {
    f.obj { o =>
      o.list { l =>
        emitFieldsWithNoArguments(l)
        emitFieldsWithArguments(l)
      }
    }
  }

  private def renderInheritance(): String = {
    if (implementsInterfaces()) {
      val interfacesNames = node.effectiveInherits.map(_.name.value())
      s"implements ${interfacesNames.mkString(" & ")}"
    } else {
      ""
    }
  }

  private def checkObjectType(): String       = if (isInterface) "interface" else "type"
  private def isInterface                     = node.isAbstract.option().getOrElse(false)
  private def implementsInterfaces(): Boolean = node.effectiveInherits.nonEmpty
}
