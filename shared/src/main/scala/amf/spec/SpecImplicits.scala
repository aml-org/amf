package amf.spec

import amf.metadata.Field
import scala.language.implicitConversions

/**
  * Spec implicits
  */
protected object SpecImplicits {

  implicit def node(symbol: Symbol): SpecKeyNode = SpecKeyNode(symbol)

  implicit def regex(regex: String): SpecRegexNode = SpecRegexNode(regex)

  implicit def field(field: Field): FieldLike = FieldLike(field)
}
