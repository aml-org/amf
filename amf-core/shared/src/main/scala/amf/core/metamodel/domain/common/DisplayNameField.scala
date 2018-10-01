package amf.core.metamodel.domain.common

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.vocabulary.Namespace.Schema

/**
  * DisplayName field.
  */
trait DisplayNameField {
  val DisplayName = Field(Str, Schema + "displayName")
}

object DisplayNameField extends DisplayNameField