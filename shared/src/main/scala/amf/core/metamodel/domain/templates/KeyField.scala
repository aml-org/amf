package amf.core.metamodel.domain.templates

import amf.core.metamodel.{Field, Obj}

/**
  * Determines a key field for merging.
  */
trait KeyField extends Obj {

  val key: Field

}
