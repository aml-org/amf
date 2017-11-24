package amf.framework.metamodel

import amf.framework.vocabulary.ValueType

/**
  * Field
  */
case class Field(`type`: Type, value: ValueType, jsonldField: Boolean = true) {
  override def toString: String = value.iri()
}
