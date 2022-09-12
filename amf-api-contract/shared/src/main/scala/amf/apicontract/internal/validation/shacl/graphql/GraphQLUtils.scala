package amf.apicontract.internal.validation.shacl.graphql

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.{EndPoint, Parameter}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{DomainElement, ScalarNode, Shape}
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter}
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.annotations.InputTypeField

import scala.annotation.tailrec

object GraphQLUtils {
  def datatype(shape: Shape): Option[String] = {
    shape match {
      case u: UnionShape => // nullable type
        u.anyOf.collectFirst { case s: ScalarShape => GraphQLDataTypes.coercedFrom(s) }
      case s: ScalarShape => Some(GraphQLDataTypes.coercedFrom(s))
      case n: NodeShape   => n.name.option()
      case _              => None
    }
  }

  def isValidInputType(schema: Shape): Boolean = {
    schema match {
      case a: ArrayShape => isValidInputType(a.items)
      case n: NodeShape  => GraphQLObject(n).isInput
      case u: UnionShape => GraphQLNullable(u).isValidInput
      case _             => true
    }
  }

  @tailrec
  def isValidOutputType(schema: Shape): Boolean = {
    schema match {
      case u: UnionShape   => GraphQLNullable(u).isValidOutput
      case n: NodeShape    => !GraphQLObject(n).isInput
      case arr: ArrayShape => isValidOutputType(arr.items)
      case _               => true
    }
  }

  def inferGraphQLKind(element: DomainElement, appliedToDirectiveArgument: Boolean): String = {
    element match {
      case s: ScalarShape if s.values.nonEmpty                                 => "enum"
      case _: ScalarShape                                                      => "scalar"
      case _: ScalarNode                                                       => "enum value"
      case n: NodeShape if n.isAbstract.value()                                => "interface"
      case n: NodeShape if n.isInputOnly.value()                               => "input object"
      case _: NodeShape                                                        => "object"
      case _: UnionShape                                                       => "union"
      case _: WebApi                                                           => "schema"
      case f: PropertyShape if f.annotations.contains(classOf[InputTypeField]) => "input field"
      case _: Parameter                                                        => "argument"
      case _: ShapeParameter                                                   => "argument"
      case _: PropertyShape if appliedToDirectiveArgument                      => "argument"
      case _: ShapeOperation                                                   => "field"
      case _: PropertyShape                                                    => "field"
      case _: EndPoint                                                         => "field"
      case _                                                                   => "type" // should be unreachable
    }
  }

  def locationFor(kind: String): String = {
    val locationByGraphQLKind: Map[String, String] = Map[String, String](
      "schema"       -> "SCHEMA",
      "scalar"       -> "SCALAR",
      "object"       -> "OBJECT",
      "field"        -> "FIELD_DEFINITION",
      "argument"     -> "ARGUMENT_DEFINITION",
      "interface"    -> "INTERFACE",
      "union"        -> "UNION",
      "enum"         -> "ENUM",
      "enum value"   -> "ENUM_VALUE",
      "input object" -> "INPUT_OBJECT",
      "input field"  -> "INPUT_FIELD_DEFINITION"
    )

    locationByGraphQLKind.getOrElse(kind, "INVALID LOCATION")
  }
}
