package amf.core.metamodel

import amf.core.vocabulary.ValueType

/**
  * Field
  */
case class Field(`type`: Type, value: ValueType, jsonldField: Boolean = true) {
  override def toString: String = value.iri()
}
