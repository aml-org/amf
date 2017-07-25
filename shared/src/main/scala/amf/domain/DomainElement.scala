package amf.domain

import amf.builder.Builder

/**
  * Internal model for any domain element
  */
trait DomainElement extends FieldsInstance {

  type T

  def toBuilder: Builder
}

/**
  * Domain model of type raml-doc:RootDomainElement
  */
trait RootDomainElement

trait FieldsInstance {
  val fields: Fields
}
