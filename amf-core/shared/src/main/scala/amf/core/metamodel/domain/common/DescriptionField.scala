package amf.core.metamodel.domain.common

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.vocabulary.Namespace.Schema

/**
  * Description field.
  */
trait DescriptionField {
  val Description = Field(Str, Schema + "description")
}

object DescriptionField extends DescriptionField