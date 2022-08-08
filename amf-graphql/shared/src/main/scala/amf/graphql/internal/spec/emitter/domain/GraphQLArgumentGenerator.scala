package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.model.domain.ScalarNode
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext

case class GeneratedGraphQLArgument(documentation: Option[String], value: String)

case class GraphQLArgumentGenerator(param: Parameter, ctx: GraphQLEmitterContext) extends GraphQLEmitter {

  def generate(): GeneratedGraphQLArgument = {
    val name       = param.name.value()
    val targetName = typeTarget(param.schema)
    val effectiveTargetName = if (param.required.option().getOrElse(false)) {
      targetName
    } else {
      cleanNonNullable(targetName)
    }

    val documentation = param.description.option()
    val defaultValue  = Option(param.defaultValue)
    val maybeDefaultValue = if (defaultValue.isDefined) {
      val effectiveValue = defaultValue.get.asInstanceOf[ScalarNode].value.value()
      s" = $effectiveValue"
    } else ""
    val argumentDefinition = s"$name: $effectiveTargetName" ++ maybeDefaultValue
    GeneratedGraphQLArgument(documentation, argumentDefinition)
  }

}
