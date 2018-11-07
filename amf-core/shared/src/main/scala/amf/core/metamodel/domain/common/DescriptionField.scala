package amf.core.metamodel.domain.common

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.{ExternalModelVocabularies, ModelDoc}
import amf.core.vocabulary.Namespace.Schema

/**
  * Description field.
  */
trait DescriptionField {
  val Description = Field(
    Str,
    Schema + "description",
    ModelDoc(ExternalModelVocabularies.SchemaOrg, "description", "Human readable description of an element"))
}

object DescriptionField extends DescriptionField
