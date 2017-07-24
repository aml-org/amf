package amf.domain

import amf.builder.Builder

/**
  * Internal model for any domain element
  */
trait DomainElement {

  type T

  def toBuilder: Builder
}

/**
  * Domain model of type raml-doc:RootDomainElement
  */
trait RootDomainElement

abstract class FieldHolder(val fields: Fields) {}
