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

  val Data = Namespace("http://raml.org/vocabularies/data#")

  val SourceMaps = Namespace("http://raml.org/vocabularies/document-source-maps#")

  val Shacl = Namespace("http://www.w3.org/ns/shacl#")

  val Schema = Namespace("http://schema.org/")

  val Hydra = Namespace("http://www.w3.org/ns/hydra/core#")

  val Xsd = Namespace("http://www.w3.org/2001/XMLSchema#")

  val AnonShapes = Namespace("http://raml.org/vocabularies/shapes/anon#")

  val Rdf = Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#")

  // To build full URIs without namespace
  val WihtoutNamespace = Namespace("")

  val Meta = Namespace("http://raml.org/vocabularies/meta#")

  val Owl = Namespace("http://www.w3.org/2002/07/owl#")

  val Rdfs = Namespace("http://www.w3.org/2000/01/rdf-schema#")

}

/** Value type. */
case class ValueType(ns: Namespace, name: String) {
  def iri(): String = ns.base + name
}

class UriType(id: String) extends ValueType(Namespace.Document,""){
  override def iri(): String = id
}

object ValueType {
  def apply(ns: Namespace, name: String) = new ValueType(ns, name)
  def apply(iri: String) = new UriType(iri)
}

