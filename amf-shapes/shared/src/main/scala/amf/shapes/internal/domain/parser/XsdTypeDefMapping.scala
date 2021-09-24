package amf.shapes.internal.domain.parser

import amf.core.client.scala.model.DataType
import amf.shapes.internal.spec.common.TypeDef._
import amf.shapes.internal.spec.common.TypeDef
import org.yaml.model.YType

/**
  * XSD TypeDef mapping
  */
object XsdTypeDefMapping {

  def xsd(typeDef: TypeDef): String =
    typeDef match {
      case StrType          => DataType.String
      case IntType          => DataType.Integer
      case LongType         => DataType.Long
      case FloatType        => DataType.Float
      case DoubleType       => DataType.Double
      case NumberType       => DataType.Number
      case BoolType         => DataType.Boolean
      case DateTimeType     => DataType.DateTime
      case DateTimeOnlyType => DataType.DateTimeOnly // custom scalar type
      case TimeOnlyType     => DataType.Time
      case DateOnlyType     => DataType.Date
      case ByteType         => DataType.Byte
      case BinaryType       => DataType.Binary
      case PasswordType     => DataType.Password // custom scalar type
      case _                => throw new RuntimeException("Unknown mapping")
    }

  /** for 0.8 */
  def xsdFromString(text: String): (Option[String], Option[String]) =
    text match {
      case "string"                      => (Some(DataType.String), Some(""))
      case "number" | "float" | "double" => (Some(DataType.Number), Some(""))
      case "integer"                     => (Some(DataType.Integer), Some(""))
      case "date"                        => (Some(DataType.DateTime), Some("RFC2616"))
      case "boolean"                     => (Some(DataType.Boolean), Some(""))
      case "file"                        => (Some(DataType.File), Some(""))
      case _                             => (None, None)

    }
}

object TypeDefYTypeMapping {

  def apply(typeDef: TypeDef): YType = typeDef match {
    case IntType | LongType | ByteType | BinaryType                    => YType.Int
    case FloatType | DoubleType | NumberType                           => YType.Float
    case BoolType                                                      => YType.Bool
    case DateTimeType | DateTimeOnlyType | TimeOnlyType | DateOnlyType => YType.Timestamp
    case _                                                             => YType.Str
  }
}
object TypeDefXsdMapping {

  def typeDef08(iri: String): String =
    iri match {
      case s if s == DataType.String   => "string"
      case s if s == DataType.Integer  => "integer"
      case s if s == DataType.Number   => "number"
      case s if s == DataType.Float    => "number"
      case s if s == DataType.Double   => "number"
      case s if s == DataType.Boolean  => "boolean"
      case s if s == DataType.DateTime => "date"
      case s if s == DataType.File     => "file"
      case s                           => throw new RuntimeException(s"Unknown mapping: $s")
    }

  def type08Def(iri: String): TypeDef =
    iri match {
      case s if s == DataType.String   => StrType
      case s if s == DataType.Integer  => IntType
      case s if s == DataType.Float    => FloatType
      case s if s == DataType.Number   => NumberType
      case s if s == DataType.Boolean  => BoolType
      case s if s == DataType.DateTime => DateTimeType
      case s if s == DataType.File     => FileType
      case s                           => throw new RuntimeException(s"Unknown mapping: $s")
    }

  def typeDef(iri: String): TypeDef =
    iri match {
      case s if s == DataType.String       => StrType
      case s if s == DataType.Decimal      => FloatType
      case s if s == DataType.Integer      => IntType
      case s if s == DataType.Long         => LongType
      case s if s == DataType.Float        => FloatType
      case s if s == DataType.Double       => DoubleType
      case s if s == DataType.Number       => NumberType
      case s if s == DataType.Boolean      => BoolType
      case s if s == DataType.DateTime     => DateTimeType
      case s if s == DataType.DateTimeOnly => DateTimeOnlyType
      case s if s == DataType.Time         => TimeOnlyType
      case s if s == DataType.Date         => DateOnlyType
      case s if s == DataType.Byte         => ByteType
      case s if s == DataType.Binary       => BinaryType
      case s if s == DataType.Password     => PasswordType
      case s if s == DataType.Nil          => NilType
      case _                               => UndefinedType
    }

  def typeDef(iri: String, format: String): TypeDef = typeDef(iri) match {
    case FloatType =>
      format match {
        case "int32"  => IntType
        case "int64"  => LongType
        case "float"  => FloatType
        case "double" => DoubleType
        case _        => NumberType
      }
    case other => other
  }
}
