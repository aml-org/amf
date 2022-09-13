package amf.apicontract.internal.validation.shacl.graphql

import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain._

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
}
