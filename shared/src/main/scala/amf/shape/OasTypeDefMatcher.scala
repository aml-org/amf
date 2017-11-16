package amf.shape

import amf.shape.TypeDef._

/**
  * Oas type to [[TypeDef]] matcher
  */
object OasTypeDefMatcher {

  val knownFormats: Set[String] = Set("time-only", "date-only", "date-time", "date-time-only", "password", "byte", "binary", "int32", "float")

  def matchType(ramlType: String, format: String = ""): TypeDef = ramlType match {
    case "string" =>
      format match {
        case "time-only"      => TimeOnlyType
        case "date-only"      => DateOnlyType
        case "date-time"      => DateTimeType
        case "date-time-only" => DateTimeOnlyType
        case "password"       => PasswordType
        case "byte"           => ByteType
        case "binary"         => BinaryType
        case _                => StrType
      }
    case "null"          => NilType
    case "integer"       => format match {
      case _       => IntType
    }
    case "number" =>
      format match {
        case "float" => FloatType
        case _       => FloatType
      }
    case "boolean"       => BoolType
    case "object"        => ObjectType
    case "array"         => ArrayType
    case "file"          => FileType
    case _               => ObjectType
  }
}

object OasTypeDefStringValueMatcher {

  def matchType(typeDef: TypeDef): String = typeDef match {
    case ByteType         => "string"
    case BinaryType       => "string"
    case PasswordType     => "string"
    case StrType          => "string"
    case IntType          => "integer"
    case FloatType        => "number"
    case BoolType         => "boolean"
    case DateTimeType     => "string"
    case DateTimeOnlyType => "string"
    case TimeOnlyType     => "string"
    case DateOnlyType     => "string"
    case ArrayType        => "array"
    case ObjectType       => "object"
    case FileType         => "file"
    case NilType          => "null"
    case UndefinedType    => throw new RuntimeException("Undefined type def")
  }

  def matchFormat(typeDef: TypeDef): Option[String] = typeDef match {
    case ByteType         => Some("byte")
    case BinaryType       => Some("binary")
    case PasswordType     => Some("password")
    case DateTimeType     => Some("date-time")
    case DateTimeOnlyType => Some("date-time-only")
    case TimeOnlyType     => Some("time-only")
    case DateOnlyType     => Some("date-only")
    case _                => None
  }
}
