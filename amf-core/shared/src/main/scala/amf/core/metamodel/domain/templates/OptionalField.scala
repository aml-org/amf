package amf.core.metamodel.domain.templates

import amf.core.metamodel.{Field, Obj}

/**
  * Determines if the field is optional for merging.
  */
trait OptionalField extends Obj {

  val optional: Field

}
