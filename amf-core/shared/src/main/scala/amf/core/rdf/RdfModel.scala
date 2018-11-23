package amf.core.rdf

import org.mulesoft.common.io.Output

class PropertyObject(val value: String)
case class Literal(override val value: String, literalType: Option[String]) extends PropertyObject(value)
case class Uri(override val value: String)                                  extends PropertyObject(value)
case class Node(subject: String, classes: Seq[String], private val properties: Map[String, Seq[PropertyObject]]) {

  def getProperties(iri: String): Option[Seq[PropertyObject]] = {
    properties.get(iri).map(_.sortWith((t1, t2) => (t1.value compareTo t2.value) > 0))
  }

  def getKeys(): Seq[String] = {
    properties.keys.toSeq.sortWith((t1, t2) => { (t1 compare t2) > 0 })
  }
}

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
    * @param writer writer where to send the representation
    * @return
    */
  def serializeWriter[W: Output](mediaType: String, writer: W): Option[W]

  def toN3(): String

  // returns the native representation of the model
  def native(): Any
}
