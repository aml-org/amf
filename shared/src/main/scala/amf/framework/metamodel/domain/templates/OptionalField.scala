package amf.framework.metamodel.domain.templates

import amf.framework.metamodel.{Field, Obj}

/**
  * Determines if the field is optional for merging.
  */
trait OptionalField extends Obj {

  val optional: Field

}
