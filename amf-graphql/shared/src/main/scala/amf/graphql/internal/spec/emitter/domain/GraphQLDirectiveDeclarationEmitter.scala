package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.internal.validation.shacl.graphql.GraphQLLocationHelper
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.helpers.GraphQLEmitterHelper.emitArgumentsWithDescriptions
import amf.graphql.internal.spec.emitter.helpers.LineEmitter
import GraphQLLocationHelper.toLocationName
import amf.shapes.client.scala.model.domain.NodeShape

case class GraphQLDirectiveDeclarationEmitter(
    directive: CustomDomainProperty,
    ctx: GraphQLEmitterContext,
    b: StringDocBuilder
) extends GraphQLEmitter {
  def emit(): Unit = {
    emitDescription()

    val name        = directive.name.value()
    val arguments   = collectArguments()
    val locations   = collectLocations()
    val repeatable  = if (directive.repeatable.value()) "repeatable" else ""
    val isMultiline = arguments.exists(_.documentation.nonEmpty)

    b.fixed { f =>
      if (isMultiline) {
        LineEmitter(f, "directive", s"@$name(").emit()
        emitArgumentsWithDescriptions(arguments, f, ctx)
        LineEmitter(f, ")", repeatable, "on", locations).emit()
      } else {
        val inputValuesDefinition = arguments.map(_.value).mkString(", ")
        val argumentsDefinition   = if (hasArguments(arguments)) s"($inputValuesDefinition)" else ""
        LineEmitter(f, "directive", s"@$name$argumentsDefinition", repeatable, "on", locations).emit()
      }
    }
  }

  private def hasArguments(arguments: Seq[GeneratedGraphQLArgument]) = arguments.nonEmpty

  private def emitDescription(): Unit = {
    val description = directive.description.option()
    GraphQLDescriptionEmitter(description, ctx, b, Some(pos(directive.annotations))).emit()
  }

  private def collectLocations(): String = {
    directive.domain
      .flatMap { fieldValue =>
        val locationIri = fieldValue.value()
        toLocationName(locationIri)
      }
      .mkString(" | ")
  }

  private def collectArguments(): Seq[GeneratedGraphQLArgument] = {
    val arguments = directive.schema.asInstanceOf[NodeShape].properties
    arguments.map { arg => GraphQLDirectiveArgumentGenerator(arg, ctx).generate() }
  }
}
