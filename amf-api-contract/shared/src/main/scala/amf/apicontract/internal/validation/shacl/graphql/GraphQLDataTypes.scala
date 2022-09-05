package amf.apicontract.internal.validation.shacl.graphql

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.ScalarNode
import amf.shapes.client.scala.model.domain.ScalarShape

object GraphQLDataTypes {

  def from(scalar: ScalarShape): String = {
    scalar.dataType.value() match {
      case DataType.Integer => "Int"
      case DataType.Float   => "Float"
      case DataType.Boolean => "Boolean"
      case DataType.Any     => scalar.format.value()
      case _                => "String"
    }
  }

  def coercedFrom(scalar: ScalarShape): String = {
    scalar.dataType.value() match {
      case DataType.Integer => "Int"
      case DataType.Float   => "Float"
      case DataType.Boolean => "Boolean"
      case _                => "String"
    }
  }

  def from(scalar: ScalarNode): String = {
    scalar.dataType.value() match {
      case DataType.Integer => "Int"
      case DataType.Float   => "Float"
      case DataType.Boolean => "Boolean"
      case _                => "String"
    }
  }

}
