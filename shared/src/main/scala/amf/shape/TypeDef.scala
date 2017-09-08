package amf.shape

import amf.shape.TypeDef._

/**
  * Type definition
  */
trait TypeDef {

  def isUndefined: Boolean = this == UndefinedType

  def isScalar: Boolean = this match {
    case _: ScalarType => true
    case _             => false
  }
}

trait ScalarType

object TypeDef {
  object StrType          extends TypeDef with ScalarType
  object IntType          extends TypeDef with ScalarType
  object FloatType        extends TypeDef with ScalarType
  object BoolType         extends TypeDef with ScalarType
  object DateTimeType     extends TypeDef with ScalarType
  object DateTimeOnlyType extends TypeDef with ScalarType
  object TimeOnlyType     extends TypeDef with ScalarType
  object DateOnlyType     extends TypeDef with ScalarType
  object ByteType         extends TypeDef with ScalarType
  object BinaryType       extends TypeDef with ScalarType
  object PasswordType     extends TypeDef with ScalarType
  object ObjectType       extends TypeDef
  object UndefinedType    extends TypeDef
}
