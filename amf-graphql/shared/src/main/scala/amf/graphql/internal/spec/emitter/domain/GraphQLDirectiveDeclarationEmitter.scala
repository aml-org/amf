package amf.graphql.internal.spec.emitter.domain
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.annotations.GraphQLLocation
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.helpers.LineEmitter
import amf.shapes.client.scala.model.domain.NodeShape

case class GraphQLDirectiveDeclarationEmitter(
    directive: CustomDomainProperty,
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder
) extends GraphQLEmitter {
  def emit(): Unit = {
    emitDescription()

    val name                  = directive.name.value()
    val arguments             = collectArguments()
    val locations             = collectLocations()
    val inputValuesDefinition = arguments.mkString(", ")
    val argumentsDefinition   = if (hasArguments(arguments)) s"($inputValuesDefinition)" else ""

    b.fixed { f => LineEmitter(f, "directive", s"@$name$argumentsDefinition", "on", locations).emit() }
  }

  private def hasArguments(arguments: Seq[String]) = arguments.nonEmpty

  private def emitDescription(): Unit = {
    val description = directive.description.option()
    GraphQLDescriptionEmitter(description, ctx, b, Some(pos(directive.annotations))).emit()
  }

  private def collectLocations(): String = {
    directive.annotations.find(classOf[GraphQLLocation]) match {
      case Some(locations) => locations.location.mkString(" | ")
      case _               => ""
    }
  }

  private def collectArguments(): Seq[String] = {
    val arguments = directive.schema.asInstanceOf[NodeShape].properties
    arguments.map { arg => GraphQLDirectiveArgumentGenerator(arg, ctx).generate().value }
  }
}
