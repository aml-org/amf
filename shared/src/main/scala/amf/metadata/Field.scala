package amf.metadata

import amf.vocabulary.ValueType

/**
  * Field
  */
case class Field(`type`: Type, value: ValueType,val jsonldField:Boolean=true) {
  override def toString: String = value.iri()
}
