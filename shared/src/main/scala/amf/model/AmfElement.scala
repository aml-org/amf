package amf.model

import amf.domain.Fields

/**
  * Amf element including DomainElements and BaseUnits
  */
trait AmfElement {

  /** Set of fields composing object. */
  val fields: Fields

  /** Element unique identifier .*/
//  val id: String
}
