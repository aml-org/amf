package amf.apicontract.internal.validation.shacl.graphql

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.{EndPoint, Parameter}
import amf.apicontract.internal.validation.shacl.graphql.GraphQLLocations.{
  ArgumentDefinition,
  Enum,
  EnumValue,
  Field,
  FieldDefinition,
  FragmentDefinition,
  FragmentSpread,
  GraphQLLocation,
  InlineFragment,
  InputFieldDefinition,
  InputObject,
  Interface,
  Mutation,
  Object,
  Query,
  Scalar,
  Schema,
  Subscription,
  Union,
  VariableDefinition
}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{DomainElement, ScalarNode}
import amf.core.client.scala.vocabulary.ValueType
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape, UnionShape}
import amf.shapes.internal.annotations.InputTypeField

object GraphQLLocationHelper {
  private lazy val all: Seq[GraphQLLocation] = Seq(
    Scalar,
    Object,
    FieldDefinition,
    ArgumentDefinition,
    Interface,
    Union,
    Enum,
    EnumValue,
    InputObject,
    InputFieldDefinition,
    Schema,
    Query,
    Mutation,
    Subscription,
    Field,
    FragmentDefinition,
    FragmentSpread,
    InlineFragment,
    VariableDefinition
  )

  // for performance
  private lazy val indexByName: Map[String, ValueType] = all.map(loc => loc.name -> loc.iri).toMap
  private lazy val indexByIri: Map[String, String]     = all.map(loc => loc.iri.iri() -> loc.name).toMap

  def toLocationIri(locationName: String): Option[String] = indexByName.get(locationName).map(_.iri())
  def toLocationIris(locationNames: String*): Seq[String] = locationNames.flatMap(indexByName.get(_).map(_.iri()))
  def toLocationName(locationIri: String): Option[String] = indexByIri.get(locationIri)
  def toLocation(element: DomainElement, appliedToDirectiveArgument: Boolean): Option[GraphQLLocation] = {
    element match {
      case s: ScalarShape if s.values.nonEmpty                                 => Some(GraphQLLocations.Enum)
      case _: ScalarShape                                                      => Some(GraphQLLocations.Scalar)
      case _: ScalarNode                                                       => Some(GraphQLLocations.EnumValue)
      case n: NodeShape if n.isAbstract.value()                                => Some(GraphQLLocations.Interface)
      case n: NodeShape if n.isInputOnly.value()                               => Some(GraphQLLocations.InputObject)
      case _: NodeShape                                                        => Some(GraphQLLocations.Object)
      case _: UnionShape                                                       => Some(GraphQLLocations.Union)
      case _: WebApi                                                           => Some(GraphQLLocations.Schema)
      case f: PropertyShape if f.annotations.contains(classOf[InputTypeField]) => Some(GraphQLLocations.InputFieldDefinition)
      case _: Parameter                                                        => Some(GraphQLLocations.ArgumentDefinition)
      case _: ShapeParameter                                                   => Some(GraphQLLocations.ArgumentDefinition)
      case _: PropertyShape if appliedToDirectiveArgument                      => Some(GraphQLLocations.ArgumentDefinition)
      case _: ShapeOperation                                                   => Some(GraphQLLocations.FieldDefinition)
      case _: PropertyShape                                                    => Some(GraphQLLocations.FieldDefinition)
      case _: EndPoint                                                         => Some(GraphQLLocations.FieldDefinition)
      case _                                                                   => None // should be unreachable
    }
  }
}
