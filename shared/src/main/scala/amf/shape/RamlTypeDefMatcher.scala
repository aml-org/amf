package amf.shape

import amf.shape.TypeDef._

/**
  * Raml type to [[TypeDef]] matcher
  */
object RamlTypeDefMatcher {

  def matchType(ramlType: String, format: String = ""): TypeDef =
    ramlType match {
      case XMLSchema(_)      => XMLSchemaType
      case JSONSchema(_)     => JSONSchemaType
      case TypeExpression(_) => TypeExpressionType
      case "nil" | ""        => NilType
      case "any"             => AnyType
      case "string" =>
        format match {
          case "byte"     => ByteType
          case "binary"   => BinaryType
          case "password" => PasswordType
          case _          => StrType
        }
      case "integer"       => IntType
      case "number"        => FloatType
      case "boolean"       => BoolType
      case "datetime"      => DateTimeType
      case "datetime-only" => DateTimeOnlyType
      case "time-only"     => TimeOnlyType
      case "date-only"     => DateOnlyType
      case "array"         => ArrayType
      case "object"        => ObjectType
      case "union"         => UnionType
      case _               => ObjectType
    }

  object XMLSchema {
    def unapply(str: String): Option[String] =
      if (str.startsWith("<")) Some(str)
      else None
  }

  object JSONSchema {
    def unapply(str: String): Option[String] =
      if (str.startsWith("[") || str.startsWith("{")) Some(str)
      else None
  }

  object TypeExpression {
    def unapply(str: String): Option[String] =
      if ((str.contains("[") && !str.startsWith("[")) || str.contains("|") || str.contains("(") || str.contains("]") || str
            .contains(")"))
        Some(str)
      else None
  }
}

object RamlTypeDefStringValueMatcher {

  def matchType(typeDef: TypeDef): (String, String) = typeDef match {

    case ByteType         => ("string", "byte")
    case BinaryType       => ("string", "binary")
    case PasswordType     => ("string", "password")
    case StrType          => ("string", "")
    case IntType          => ("integer", "")
    case FloatType        => ("number", "")
    case BoolType         => ("boolean", "")
    case DateTimeType     => ("datetime", "")
    case DateTimeOnlyType => ("datetime-only", "")
    case TimeOnlyType     => ("time-only", "")
    case DateOnlyType     => ("date-only", "")
    case ObjectType       => ("object", "")
    case UndefinedType    => throw new RuntimeException("Undefined type def")
  }
}
