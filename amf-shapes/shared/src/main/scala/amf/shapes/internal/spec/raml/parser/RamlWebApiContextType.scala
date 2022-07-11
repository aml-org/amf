package amf.shapes.internal.spec.raml.parser

object RamlWebApiContextType extends Enumeration {
  type RamlWebApiContextType = Value
  val DEFAULT, RESOURCE_TYPE, TRAIT, EXTENSION, OVERLAY, LIBRARY = Value
}
