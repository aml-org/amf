package amf.core.metamodel.domain.common

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.vocabulary.Namespace.{Schema, Shacl}

/**
  * Name field.
  */
trait NameFieldSchema {
  val Name = Field(Str, Schema + "name")
}

trait NameFieldShacl {
  val Name = Field(Str, Shacl + "name")
}

object NameFieldSchema extends NameFieldSchema

object NameFieldShacl extends NameFieldShacl