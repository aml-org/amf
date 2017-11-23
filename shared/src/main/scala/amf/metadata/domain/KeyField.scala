package amf.metadata.domain

import amf.framework.metamodel.{Field, Obj}

/**
  * Determines a key field for merging.
  */
trait KeyField extends Obj {

  val key: Field

}
