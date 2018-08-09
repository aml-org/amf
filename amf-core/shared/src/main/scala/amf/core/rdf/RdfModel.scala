package amf.core.rdf
import java.io.Writer

class PropertyObject(val value: String)
case class Literal(override val value: String, literalType: Option[String]) extends PropertyObject(value)
case class Uri(override val value: String) extends PropertyObject(value)
case class Node(subject: String, classes: Seq[String], properties: Map[String, Seq[PropertyObject]])

/**
  * Base class for all the RDF native models in different platforms
  */
abstract class RdfModel {
  var anonCounter = 0

  def addTriple(subject: String, predicate: String, objResource: String): RdfModel
  def addTriple(subject: String, predicate: String, objLiteralValue: String, objLiteralType: Option[String]): RdfModel
  def findNode(uri: String): Option[Node]

  def nextAnonId(): String = synchronized {
    anonCounter += 1
    s"http://amf.org/anon/$anonCounter"
  }

  /**
  * Load RDF string representation in this model
    * @param text
    * @param mediaType
    */
  def load(mediaType: String, text: String)

  /**
    * Write model as a String representation
    * @param mediaType
    * @return
    */
  def serializeString(mediaType: String): Option[String]

  /**
    * Write model using a writer
    * @param mediaType
    * @writer writer where to send the representation
    * @return
    */
  def serializeWriter(mediaType: String, writer: Writer): Option[Writer]

  def toN3(): String

  // returns the native representation of the model
  def native(): Any
}
