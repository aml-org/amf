package amf.model

import amf.builder.Builder

/**
  * Domain model of type raml-doc:DomainElement
  */
trait DomainElement[T <: DomainElement[T, B], B <: Builder[T]] {
  def toBuilder: B
}

/**
  * Domain model of type raml-doc:RootDomainElement
  */
trait RootDomainElement
