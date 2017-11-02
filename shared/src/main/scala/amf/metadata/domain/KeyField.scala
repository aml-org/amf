package amf.metadata.domain

import amf.metadata.{Field, Obj}

/**
  * Determines a key field for merging.
  */
trait KeyField extends Obj {

  val key: Field

}
