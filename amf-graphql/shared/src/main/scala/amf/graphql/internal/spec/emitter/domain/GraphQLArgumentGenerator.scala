package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.model.domain.ScalarNode
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext

case class GeneratedGraphQLArgument(documentation: Option[String], value: String)

case class GraphQLArgumentGenerator(param: Parameter, ctx: GraphQLEmitterContext) extends GraphQLEmitter {

  def generate(): GeneratedGraphQLArgument = {
    val name         = param.name.value()
    val argumentType = extractGraphQLType
    val description  = param.description.option()

    val argumentDefinition = defaultValue match {
      case Some(defaultValue) => s"$name: $argumentType = $defaultValue"
      case _                  => s"$name: $argumentType"
    }

    GeneratedGraphQLArgument(description, argumentDefinition)
  }

  private def extractGraphQLType: String = {
    val argumentType = typeTarget(param.schema)
    if (param.required.option().getOrElse(false)) argumentType else cleanNonNullable(argumentType)
  }

  private def defaultValue: Option[String] = {
    param.defaultValue match {
      case defaultValueNode: ScalarNode => Some(defaultValueNode.value.value())
      case _                            => None
    }
  }
}
