package amf.spec

import amf.metadata.Field

/**
  * Spec implicits
  */
protected object SpecImplicits {

  implicit def node(symbol: Symbol): SpecNode = SpecNode(symbol)

  implicit def field(field: Field): FieldLike = FieldLike(field)
}
