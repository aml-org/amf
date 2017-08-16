package amf.metadata

import amf.vocabulary.ValueType

/**
  * Field
  */
case class Field(`type`: Type, value: ValueType) {
  override def toString: String = value.iri()
}
