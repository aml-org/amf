package amf.shape

import amf.shape.TypeDef._

/**
  * Raml type to [[TypeDef]] matcher
  */
object RamlTypeDefMatcher {

  def matchType(ramlType: String, format: String = ""): TypeDef = ramlType match {
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
    case "object"        => ObjectType
    case _               => UndefinedType
  }
}
