package amf.graphql.internal.spec.emitter.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext

case class GraphQLPropertyFieldEmitter(property: PropertyShape, ctx: GraphQLEmitterContext, b: StringDocBuilder)
    extends GraphQLEmitter {

  def emit(): Object = {
    val name           = property.name.value()
    val nullable       = property.minCount.option().getOrElse(0) == 0
    val range          = typeTarget(property.range)
    val effectiveRange = if (nullable) cleanNonNullable(range) else range
    val fieldString    = s"$name : $effectiveRange"
    property.description.option() match {
      case Some(desc) =>
        b.fixed { f =>
          GraphQLDescriptionEmitter(Some(desc), ctx, f, Some(pos(property.annotations))).emit()
          f.+=(fieldString)
        }
      case _ =>
        b.+=(fieldString, pos(property.annotations))
    }
  }

}
