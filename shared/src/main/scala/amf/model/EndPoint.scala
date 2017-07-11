package amf.model

import amf.builder.EndPointBuilder
import amf.metadata.model.EndPointModel._

/**
  * Domain model of type raml-http:EndPoint
  *
  * Properties ->
  *     - raml-http:path
  *     - schema-org:name
  *     - schema-org:description
  */
class EndPoint(val fields: Fields, val nodePath: String, val children: List[EndPoint] = List())
    extends DomainElement[EndPoint, EndPointBuilder] {
  val name: String        = fields get Name
  val description: String = fields get Description
  val path: String        = fields get Path

  override def toBuilder: EndPointBuilder =
    EndPointBuilder().copy(fields).withPath(nodePath).withChildren(children.map(_.toBuilder))

  def canEqual(other: Any): Boolean = other.isInstanceOf[EndPoint]

  override def equals(other: Any): Boolean = other match {
    case that: EndPoint =>
      (that canEqual this) &&
        name == that.name &&
        description == that.description &&
        path == that.path &&
        nodePath == that.nodePath &&
        (children zip that.children).exists { case (a, b) => a != b }
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name, description, path, nodePath, children)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object EndPoint {
  def apply(fields: Fields, nodePath: String, children: List[EndPoint] = List()): EndPoint =
    new EndPoint(fields, nodePath, children)
}
