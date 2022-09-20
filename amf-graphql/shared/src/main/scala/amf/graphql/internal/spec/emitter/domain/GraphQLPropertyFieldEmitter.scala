package amf.graphql.internal.spec.emitter.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.helpers.{LineEmitter, StringBuilder}

case class GraphQLPropertyFieldEmitter(property: PropertyShape, ctx: GraphQLEmitterContext, b: StringDocBuilder)
    extends GraphQLEmitter {

  def emit(): Unit = {
    val name         = property.name.value()
    val nullable     = property.minCount.option().getOrElse(0) == 0
    val range        = typeTarget(property.range)
    val returnedType = extractGraphQLType(nullable, range)
    val directives   = GraphQLDirectiveApplicationsRenderer(property)
    val fieldString  = StringBuilder(s"$name:", returnedType, directives)

    property.description.option() match {
      case Some(description) =>
        b.fixed { f =>
          GraphQLDescriptionEmitter(Some(description), ctx, f, Some(pos(property.annotations))).emit()
          LineEmitter(f, fieldString).emit()
        }
      case _ => LineEmitter(b, fieldString).emit()
    }
  }

  private def extractGraphQLType(nullable: Boolean, range: String) = if (nullable) cleanNonNullable(range) else range
}
