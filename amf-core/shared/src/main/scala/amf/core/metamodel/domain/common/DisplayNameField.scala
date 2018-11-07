package amf.core.metamodel.domain.common

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ExternalModelVocabularies, ModelDoc}
import amf.core.vocabulary.Namespace.Schema

/**
  * DisplayName field.
  */
trait DisplayNameField {
  val DisplayName = Field(
    Str,
    Schema + "displayName",
    ModelDoc(ExternalModelVocabularies.SchemaOrg, "display name", "Human readable name for an entity"))
}

object DisplayNameField extends DisplayNameField
