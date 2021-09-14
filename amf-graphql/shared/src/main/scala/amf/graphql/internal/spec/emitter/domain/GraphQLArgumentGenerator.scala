package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext

case class GeneratedGraphQLArgument(documentation: Option[String], value: String)

case class GraphQLArgumentGenerator(param: Parameter, ctx: GraphQLEmitterContext) extends GraphQLEmitter {

  def generate(): GeneratedGraphQLArgument = {
    val name = param.name.value()
    val targetName = typeTarget(param.schema)
    val effetiveTargetName = if (param.required.option().getOrElse(false)) {
      targetName
    } else {
      cleanNonNullable(targetName)
    }

    val documentation = param.description.option()
    GeneratedGraphQLArgument(documentation, s"$name: $effetiveTargetName")
  }

}
