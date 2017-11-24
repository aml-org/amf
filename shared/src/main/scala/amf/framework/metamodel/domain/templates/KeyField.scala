package amf.framework.metamodel.domain.templates

import amf.framework.metamodel.{Field, Obj}

/**
  * Determines a key field for merging.
  */
trait KeyField extends Obj {

  val key: Field

}
