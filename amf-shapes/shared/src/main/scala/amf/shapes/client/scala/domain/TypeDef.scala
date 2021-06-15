package amf.shapes.client.scala.domain

import amf.shapes.client.scala.domain.TypeDef._

/**
  * Type definition
  */
trait TypeDef {

  def isUndefined: Boolean = this == UndefinedType

  def isScalar: Boolean = this match {
    case _: ScalarType => true
    case _             => false
  }

  def isNil: Boolean = this match {
    case NilType => true
    case _       => false
  }

  def isAny: Boolean = this match {
    case AnyType => true
    case _       => false
  }

  def isNumber: Boolean = this match {
    case IntType | FloatType | LongType | DoubleType | NumberType => true
    case _                                                        => false
  }

  def isDate: Boolean = this match {
    case DateTimeType | DateTimeOnlyType | TimeOnlyType | DateOnlyType => true
    case _                                                             => false
  }
}

trait ScalarType

object TypeDef {
  object UnionType          extends TypeDef
  object AnyType            extends TypeDef
  object NilType            extends TypeDef with ScalarType
  object StrType            extends TypeDef with ScalarType
  object IntType            extends TypeDef with ScalarType
  object LongType           extends TypeDef with ScalarType
  object FloatType          extends TypeDef with ScalarType
  object NumberType         extends TypeDef with ScalarType // not use this type def, only for emit union type in shaclvalidation
  object DoubleType         extends TypeDef with ScalarType
  object BoolType           extends TypeDef with ScalarType
  object DateTimeType       extends TypeDef with ScalarType
  object DateTimeOnlyType   extends TypeDef with ScalarType
  object TimeOnlyType       extends TypeDef with ScalarType
  object DateOnlyType       extends TypeDef with ScalarType
  object ByteType           extends TypeDef with ScalarType
  object BinaryType         extends TypeDef with ScalarType
  object PasswordType       extends TypeDef with ScalarType
  object FileType           extends TypeDef with ScalarType
  object ArrayType          extends TypeDef
  object ObjectType         extends TypeDef
  object LinkType           extends TypeDef
  object UndefinedType      extends TypeDef
  object TypeExpressionType extends TypeDef
  object XMLSchemaType      extends TypeDef
  object JSONSchemaType     extends TypeDef
  object ExternalSchemaWrapper extends TypeDef
  object MultipleMatch      extends TypeDef
  object NilUnionType       extends TypeDef
}
