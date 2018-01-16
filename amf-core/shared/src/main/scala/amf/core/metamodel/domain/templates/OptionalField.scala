package amf.core.metamodel.domain.templates

import amf.core.metamodel.Type.Bool
import amf.core.metamodel.{Field, Obj}
import amf.core.vocabulary.Namespace

/**
  * Determines if the field is optional for merging.
  */
trait OptionalField extends Obj {
  val Optional = Field(Bool, Namespace.Http + "optional")
}
