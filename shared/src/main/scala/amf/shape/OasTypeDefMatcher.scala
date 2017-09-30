package amf.shape

import amf.shape.TypeDef._

/**
  * Oas type to [[TypeDef]] matcher
  */
object OasTypeDefMatcher {

  def matchType(ramlType: String, format: String = ""): TypeDef = ramlType match {
    case "string" =>
      format match {
        case "time-only" => TimeOnlyType
        case "date-only" => DateOnlyType
        case "password"  => PasswordType
        case _           => StrType
      }
    case "null"          => NilType
    case "integer"       => IntType
    case "number"        => FloatType
    case "boolean"       => BoolType
    case "byte"          => ByteType
    case "binary"        => BinaryType
    case "datetime"      => DateTimeType
    case "datetime-only" => DateTimeOnlyType
    case "object"        => ObjectType
    case "array"         => ArrayType
    case _               => ObjectType
  }
}

object OasTypeDefStringValueMatcher {

  def matchType(typeDef: TypeDef): String = typeDef match {

    case ByteType         => "byte"
    case BinaryType       => "binary"
    case PasswordType     => "string"
    case StrType          => "string"
    case IntType          => "integer"
    case FloatType        => "number"
    case BoolType         => "boolean"
    case DateTimeType     => "datetime"
    case DateTimeOnlyType => "datetime-only"
    case TimeOnlyType     => "string"
    case DateOnlyType     => "string"
    case ArrayType        => "array"
    case ObjectType       => "object"
    case UndefinedType    => throw new RuntimeException("Undefined type def")
  }
}
