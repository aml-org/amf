package amf.model

import amf.domain.Fields

/**
  * Amf element including DomainElements and BaseUnits
  */
trait AmfElement {

  /** Set of fields composing object. */
  val fields: Fields

  /** Return element unique identifier given parent id.*/
  def id(parent: String): String = ???
}
