package amf.apicontract.internal.validation.shacl.graphql

import amf.core.client.scala.vocabulary.Namespace.GraphQL
import amf.core.client.scala.vocabulary.ValueType

object GraphQLLocations {
  private val loc = "directive/location"
  case class GraphQLLocation(name: String, iri: ValueType)
  object Scalar             extends GraphQLLocation("SCALAR", GraphQL + s"$loc/SCALAR")
  object Object             extends GraphQLLocation("OBJECT", GraphQL + s"$loc/OBJECT")
  object FieldDefinition    extends GraphQLLocation("FIELD_DEFINITION", GraphQL + s"$loc/FIELD_DEFINITION")
  object ArgumentDefinition extends GraphQLLocation("ARGUMENT_DEFINITION", GraphQL + s"$loc/ARGUMENT_DEFINITION")
  object Interface          extends GraphQLLocation("INTERFACE", GraphQL + s"$loc/INTERFACE")
  object Union              extends GraphQLLocation("UNION", GraphQL + s"$loc/UNION")
  object Enum               extends GraphQLLocation("ENUM", GraphQL + s"$loc/ENUM")
  object EnumValue          extends GraphQLLocation("ENUM_VALUE", GraphQL + s"$loc/ENUM_VALUE")
  object InputObject        extends GraphQLLocation("INPUT_OBJECT", GraphQL + s"$loc/INPUT_OBJECT")
  object InputFieldDefinition
      extends GraphQLLocation("INPUT_FIELD_DEFINITION", GraphQL + s"$loc/INPUT_FIELD_DEFINITION")
  object Schema             extends GraphQLLocation("SCHEMA", GraphQL + s"$loc/SCHEMA")
  object Query              extends GraphQLLocation("QUERY", GraphQL + s"$loc/QUERY")
  object Mutation           extends GraphQLLocation("MUTATION", GraphQL + s"$loc/MUTATION")
  object Subscription       extends GraphQLLocation("SUBSCRIPTION", GraphQL + s"$loc/SUBSCRIPTION")
  object Field              extends GraphQLLocation("FIELD", GraphQL + s"$loc/FIELD")
  object FragmentDefinition extends GraphQLLocation("FRAGMENT_DEFINITION", GraphQL + s"$loc/FRAGMENT_DEFINITION")
  object FragmentSpread     extends GraphQLLocation("FRAGMENT_SPREAD", GraphQL + s"$loc/FRAGMENT_SPREAD")
  object InlineFragment     extends GraphQLLocation("INLINE_FRAGMENT", GraphQL + s"$loc/INLINE_FRAGMENT")
  object VariableDefinition extends GraphQLLocation("VARIABLE_DEFINITION", GraphQL + s"$loc/VARIABLE_DEFINITION")
}
