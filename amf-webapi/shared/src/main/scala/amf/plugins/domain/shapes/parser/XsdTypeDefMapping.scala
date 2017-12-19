package amf.plugins.domain.shapes.parser

import amf.plugins.domain.shapes.models.TypeDef
import amf.plugins.domain.shapes.models.TypeDef._
import amf.core.vocabulary.Namespace.{Shapes, Xsd}

/**
  * XSD [[TypeDef]] mapping
  */
object XsdTypeDefMapping {

  def xsd(typeDef: TypeDef): String =
    (typeDef match {
      case StrType          => Xsd + "string"
      case IntType          => Xsd + "integer"
      case LongType         => Xsd + "long"
      case FloatType        => Xsd + "float"
      case DoubleType       => Xsd + "double"
      case BoolType         => Xsd + "boolean"
      case DateTimeType     => Xsd + "dateTime"
      case DateTimeOnlyType => Shapes + "dateTimeOnly" // custom scalar type
      case TimeOnlyType     => Xsd + "time"
      case DateOnlyType     => Xsd + "date"
      case ByteType         => Xsd + "byte"
      case BinaryType       => Xsd + "base64Binary"
      case PasswordType     => Shapes + "password" // custom scalar type
      case _                => throw new RuntimeException("Unknown mapping")
    }).iri()

  /** for 0.8*/
  def xsdFromString(text: String): String =
    (text match {
      case "string"  => Xsd + "string"
      case "number"  => Xsd + "float"
      case "integer" => Xsd + "integer"
      case "date"    => Xsd + "datetime"
      case "boolean" => Xsd + "boolean"
      case "file"    => Shapes + "file"
    }).iri()
}

object TypeDefXsdMapping {

  def typeDef(iri: String): TypeDef =
    iri match {
      case s if s == (Xsd + "string").iri()          => StrType
      case s if s == (Xsd + "integer").iri()         => IntType
      case s if s == (Xsd + "long").iri()            => LongType
      case s if s == (Xsd + "float").iri()           => FloatType
      case s if s == (Xsd + "double").iri()          => DoubleType
      case s if s == (Xsd + "boolean").iri()         => BoolType
      case s if s == (Xsd + "dateTime").iri()        => DateTimeType
      case s if s == (Shapes + "dateTimeOnly").iri() => DateTimeOnlyType
      case s if s == (Xsd + "time").iri()            => TimeOnlyType
      case s if s == (Xsd + "date").iri()            => DateOnlyType
      case s if s == (Xsd + "byte").iri()            => ByteType
      case s if s == (Xsd + "base64Binary").iri()    => BinaryType
      case s if s == (Shapes + "password").iri()     => PasswordType
      case s                                         => throw new RuntimeException(s"Unknown mapping: $s")
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
