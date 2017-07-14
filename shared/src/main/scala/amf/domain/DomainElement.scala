package amf.domain

import amf.builder.Builder

/**
  * Internal model for any domain element
  */
trait DomainElement[T <: DomainElement[T, B], B <: Builder[T]] {
  def toBuilder: B
}

/**
  * Domain model of type raml-doc:RootDomainElement
  */
trait RootDomainElement
