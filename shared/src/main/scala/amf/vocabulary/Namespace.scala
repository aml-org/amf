package amf.vocabulary

/**
  * Namespaces
  */
case class Namespace(base: String) {
  def +(id: String): ValueType = ValueType(this, id)
}

object Namespace {

  val Document = Namespace("http://raml.org/vocabularies/document#")

  val Http = Namespace("http://raml.org/vocabularies/http#")

  val Shapes = Namespace("http://raml.org/vocabularies/shapes#")

  val SourceMaps = Namespace("http://raml.org/vocabularies/document-source-maps#")

  val Shacl = Namespace("http://www.w3.org/ns/shacl#")

  val Schema = Namespace("http://schema.org/")

  val Hydra = Namespace("http://www.w3.org/ns/hydra/core#")

  val Xsd = Namespace("http://www.w3.org/2001/XMLSchema#")

  val AnonShapes = Namespace("http://raml.org/vocabularies/shapes/anon#")
}

/** Value type. */
case class ValueType(ns: Namespace, name: String) {
  def iri(): String = ns.base + name
}
