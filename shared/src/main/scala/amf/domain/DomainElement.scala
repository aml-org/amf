package amf.domain

import amf.builder.Builder

/**
  * Internal model for any domain element
  */
trait DomainElement {

  type This <: DomainElement

  def toBuilder: Builder[This]
}

/**
  * Domain model of type raml-doc:RootDomainElement
  */
trait RootDomainElement
