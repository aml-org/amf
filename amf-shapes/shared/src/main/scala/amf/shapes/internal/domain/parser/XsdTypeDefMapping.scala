package amf.shapes.internal.domain.parser

import amf.core.client.scala.model.DataType
import amf.shapes.internal.spec.common.TypeDef
import amf.shapes.internal.spec.common.TypeDef._
import org.yaml.model.YType

/** XSD TypeDef mapping
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
      case PasswordType     => DataType.Password     // custom scalar type
      case _                => throw new RuntimeException("Unknown mapping")
    }

  /** for RAML 0.8 */
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
      case DataType.String   => "string"
      case DataType.Integer  => "integer"
      case DataType.Number   => "number"
      case DataType.Float    => "number"
      case DataType.Double   => "number"
      case DataType.Boolean  => "boolean"
      case DataType.DateTime => "date"
      case DataType.File     => "file"
      case s                 => throw new RuntimeException(s"Unknown mapping: $s")
    }

  def type08Def(iri: String): TypeDef =
    iri match {
      case DataType.String   => StrType
      case DataType.Integer  => IntType
      case DataType.Float    => FloatType
      case DataType.Number   => NumberType
      case DataType.Boolean  => BoolType
      case DataType.DateTime => DateTimeType
      case DataType.File     => FileType
      case s                 => throw new RuntimeException(s"Unknown mapping: $s")
    }

  def typeDef(iri: String): TypeDef =
    iri match {
      case DataType.String       => StrType
      case DataType.Integer      => IntType
      case DataType.Long         => LongType
      case DataType.Float        => FloatType
      case DataType.Double       => DoubleType
      case DataType.Number       => NumberType
      case DataType.Boolean      => BoolType
      case DataType.DateTime     => DateTimeType
      case DataType.DateTimeOnly => DateTimeOnlyType
      case DataType.Time         => TimeOnlyType
      case DataType.Date         => DateOnlyType
      case DataType.Byte         => ByteType
      case DataType.Bytes        => BytesType
      case DataType.Binary       => BinaryType
      case DataType.Password     => PasswordType
      case DataType.Nil          => NilType
      case _                     => UndefinedType
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
