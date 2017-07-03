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

/**
  * Domain model of type raml-doc:ApiDocumentation
  */
trait ApiDocumentation[T <: DomainElement[T, B], B <: Builder[T]] extends DomainElement[T, B]
