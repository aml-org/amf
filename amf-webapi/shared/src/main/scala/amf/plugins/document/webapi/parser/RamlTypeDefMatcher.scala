package amf.plugins.document.webapi.parser

import amf.plugins.domain.shapes.models.TypeDef
import amf.plugins.domain.shapes.models.TypeDef._

/**
  * Raml type to TypeDef matcher
  */
object RamlTypeDefMatcher {

  val knownFormats: Set[String] =
    Set("byte", "binary", "password", "int", "int8", "int16", "int32", "int64", "long", "double", "float")
  def match08Type(value: String): Option[TypeDef] = value match {
    case "number"  => Some(NumberType)
    case "integer" => Some(IntType)
    case "date"    => Some(DateTimeType)
    case "boolean" => Some(BoolType)
    case "file"    => Some(FileType)
    case "string"  => Some(StrType)
    case _         => None
  }

  def matchType(typeName: TypeName, default: TypeDef = ObjectType, isRef: Boolean = false): TypeDef = {
    val TypeName(ramlType, format) = typeName
    ramlType match {
      case XMLSchema(_) if !isRef      => XMLSchemaType
      case JSONSchema(_) if !isRef     => JSONSchemaType
      case TypeExpression(_) if !isRef => TypeExpressionType
      case "nil" | "" | "null"         => NilType
      case "any"                       => AnyType
      case "string" =>
        format match {
          case Some("byte")     => ByteType
          case Some("binary")   => BinaryType
          case Some("password") => PasswordType
          case _                => StrType
        }
      case "number" =>
        format match {
          case Some("int")    => IntType
          case Some("int8")   => IntType
          case Some("int16")  => IntType
          case Some("int32")  => IntType
          case Some("int64")  => LongType
          case Some("long")   => LongType
          case Some("float")  => FloatType
          case Some("double") => DoubleType
          case _              => NumberType
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
  }

  private def ltrim(s: String) =
    s.replaceAll("^(\\s+|[\uFEFF-\uFFFF])", "")

  object XMLSchema {
    def unapply(str: String): Option[String] = {
      val trimed = ltrim(str)
      if (trimed.startsWith("<") && !(trimed.startsWith("<<") && trimed.endsWith(">>"))) Some(str)
      else None
    }
  }

  object JSONSchema {
    def unapply(str: String): Option[String] = {
      val trimed = ltrim(str.trim)
      if (trimed.startsWith("[") || trimed.startsWith("{")) Some(str)
      else None
    }
  }

  object TypeExpression {
    def unapply(str: String): Option[String] = {
      val trimed = ltrim(str.trim)
      if ((trimed.contains("[]") && !trimed.startsWith("[") && trimed.endsWith("]")) || trimed.contains("|") || trimed
            .contains("(") || trimed.contains(")"))
        Some(str)
      else None
    }
  }
}

case class TypeName(typeDef: String, format: Option[String] = None)
object TypeName {
  def apply(typeDef: String, format: String): TypeName = new TypeName(typeDef, Some(format))
}

object RamlTypeDefStringValueMatcher {

  def matchType(typeDef: TypeDef, format: Option[String]): TypeName = typeDef match {

    case ByteType     => TypeName("string", "byte")
    case BinaryType   => TypeName("string", "binary")
    case PasswordType => TypeName("string", "password")
    case StrType      => TypeName("string")
    case IntType =>
      format match {
        case Some("int")   => TypeName("number", "int")
        case Some("int8")  => TypeName("number", "int8")
        case Some("int16") => TypeName("number", "int16")
        case Some("int32") => TypeName("number", "int32")
        case _             => TypeName("integer")
      }
    case LongType =>
      format match {
        case Some("int64") => TypeName("number", "int64")
        case Some("long")  => TypeName("number", "long")
        case _             => TypeName("integer", "long")
      }
    case FloatType        => TypeName("number", "float")
    case DoubleType       => TypeName("number", "double")
    case BoolType         => TypeName("boolean")
    case DateTimeType     => TypeName("datetime")
    case DateTimeOnlyType => TypeName("datetime-only")
    case TimeOnlyType     => TypeName("time-only")
    case DateOnlyType     => TypeName("date-only")
    case ArrayType        => TypeName("array")
    case ObjectType       => TypeName("object")
    case FileType         => TypeName("file")
    case NilType          => TypeName("nil")
    case NumberType       => TypeName("number")
    case UndefinedType    => throw new RuntimeException("Undefined type def")
  }
}

object RamlShapeTypeBeautifier {

  def beautify(shape: String): String = shape match {
    case "annotation"        => "annotation"
    case "anyShape"          => "any"
    case "arrayShape"        => "array"
    case "dateScalarShape"   => "date"
    case "endPoint"          => "endpoint"
    case "example"           => "example"
    case "fileShape"         => "file"
    case "module"            => "library"
    case "nodeShape"         => "object"
    case "numberScalarShape" => "number"
    case "operation"         => "operation"
    case "resourceType"      => "resource type"
    case "response"          => "response"
    case "schemaShape"       => "schema"
    case "securitySchema"    => "security schema"
    case "shape"             => "shape"
    case "stringScalarShape" => "string"
    case "trait"             => "trait"
    case "unionShape"        => "union"
    case "userDocumentation" => "documentation"
    case "webApi"            => "root"
    case "xmlSerialization"  => "xml"
    case other               => other
  }
}
