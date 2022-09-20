package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.model.domain.{DataNode, NamedDomainElement, ScalarNode}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.helpers.StringBuilder

case class GeneratedGraphQLArgument(documentation: Option[String], value: String)

case class GraphQLDirectiveArgumentGenerator(arg: PropertyShape, ctx: GraphQLEmitterContext)
    extends GraphQLArgumentGenerator(arg, ctx) {
  override def argumentType: String        = typeTarget(arg.range)
  override def description: Option[String] = arg.description.option()
  override def value: String               = getDefaultValue(arg.default)
}

case class GraphQLOperationArgumentGenerator(arg: Parameter, ctx: GraphQLEmitterContext)
    extends GraphQLArgumentGenerator(arg, ctx) {
  override def argumentType: String = {
    val graphQLType = typeTarget(arg.schema)
    if (arg.required.option().getOrElse(false)) graphQLType else cleanNonNullable(graphQLType)
  }
  override def description: Option[String] = arg.description.option()
  override def value: String               = getDefaultValue(arg.defaultValue)
}

abstract class GraphQLArgumentGenerator(arg: NamedDomainElement, ctx: GraphQLEmitterContext) extends GraphQLEmitter {

  def generate(): GeneratedGraphQLArgument = {
    val argumentDefinition = StringBuilder(s"$name:", argumentType, defaultValue, directives)
    GeneratedGraphQLArgument(description, argumentDefinition)
  }

  protected def name: String         = arg.name.value()
  protected def defaultValue: String = if (value.nonEmpty) s"= $value" else ""

  protected def getDefaultValue(node: DataNode): String = DataNodeRenderer.render(node)

  def argumentType: String
  def description: Option[String]
  def value: String
  def directives: String = GraphQLDirectiveApplicationsRenderer(arg)
}
