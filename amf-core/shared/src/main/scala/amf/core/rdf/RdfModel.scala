package amf.core.rdf

/**
  * Base class for all the RDF native models in different platforms
  */
abstract class RdfModel {
  var anonCounter = 0

  def addTriple(subject: String, predicate: String, objResource: String): RdfModel
  def addTriple(subject: String, predicate: String, objLiteralValue: String, objLiteralType: Option[String]): RdfModel

  def nextAnonId(): String = synchronized {
    anonCounter += 1
    s"http://amf.org/anon/$anonCounter"
  }

  def toN3(): String

  // returns the native representation of the model
  def native(): Any
}
