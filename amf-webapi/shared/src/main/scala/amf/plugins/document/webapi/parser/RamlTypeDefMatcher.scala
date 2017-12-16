package amf.plugins.document.webapi.parser

import amf.plugins.domain.shapes.models.TypeDef
import amf.plugins.domain.shapes.models.TypeDef._

/**
  * Raml type to [[TypeDef]] matcher
  */
object RamlTypeDefMatcher {

  val knownFormats: Set[String] = Set("byte", "binary", "password", "int32", "int64", "double", "float")

  def matchType(ramlType: String, format: String = "", default: TypeDef = ObjectType): TypeDef =
    ramlType match {
      case XMLSchema(_)        => XMLSchemaType
      case JSONSchema(_)       => JSONSchemaType
      case TypeExpression(_)   => TypeExpressionType
      case "nil" | "" | "null" => NilType
      case "any"               => AnyType
      case "string" =>
        format match {
          case "byte"     => ByteType
          case "binary"   => BinaryType
          case "password" => PasswordType
          case _          => StrType
        }
      case "number"        =>
        format match {
          case "int32"    => IntType
          case "int64"    => LongType
          case "float"    => FloatType
          case "double"   => DoubleType
          case _          => FloatType
        }
      case "integer"       => IntType
      case "boolean"       => BoolType
      case "datetime"      => DateTimeType
      case "datetime-only" => DateTimeOnlyType
      case "time-only"     => TimeOnlyType
      case "date-only"     => DateOnlyType
      case "array"         => ArrayType
      case "object"        => ObjectType
      case "union"         => UnionType
      case "file"          => FileType
      case _               => default
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
    case LongType         => ("intenger", "long")
    case FloatType        => ("number", "float")
    case DoubleType       => ("number", "dobule")
    case BoolType         => ("boolean", "")
    case DateTimeType     => ("datetime", "")
    case DateTimeOnlyType => ("datetime-only", "")
    case TimeOnlyType     => ("time-only", "")
    case DateOnlyType     => ("date-only", "")
    case ArrayType        => ("array","")
    case ObjectType       => ("object", "")
    case FileType         => ("file", "")
    case NilType          => ("nil", "")
    case UndefinedType    => throw new RuntimeException("Undefined type def")
  }
}
