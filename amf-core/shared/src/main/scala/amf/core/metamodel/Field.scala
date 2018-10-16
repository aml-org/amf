package amf.core.metamodel

import amf.core.metamodel.domain.ModelDoc
import amf.core.vocabulary.ValueType

/**
  * Field
  */
case class Field(`type`: Type, value: ValueType, doc: ModelDoc = ModelDoc(), jsonldField: Boolean = true) {
  override def toString: String = value.iri()

  override def canEqual(a: Any) = Option(a).isDefined &&  a.isInstanceOf[Field]
  override def equals(that: Any): Boolean =
    that match {
      case that: Field => that.value.iri() == value.iri()
      case _           => false
    }

}
