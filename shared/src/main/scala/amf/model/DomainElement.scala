package amf.model

import amf.builder.Builder

/**
  * Domain model base trait.
  */
trait DomainElement {
  def toBuilder[T <: DomainElement]: Builder[T]
}

trait RootDomainElement
