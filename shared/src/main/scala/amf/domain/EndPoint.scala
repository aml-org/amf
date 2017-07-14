package amf.domain

import amf.builder.EndPointBuilder
import amf.metadata.domain.EndPointModel._

/**
  * EndPoint internal model
  */
case class EndPoint(fields: Fields) extends DomainElement {

  override type This = EndPoint

  val name: String        = fields get Name
  val description: String = fields get Description
  val path: String        = fields get Path

  def canEqual(other: Any): Boolean = other.isInstanceOf[EndPoint]

  override def equals(other: Any): Boolean = other match {
    case that: EndPoint =>
      (that canEqual this) &&
        name == that.name &&
        description == that.description &&
        path == that.path
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name, description, path)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"EndPoint($name, $description, $path)"

  override def toBuilder: EndPointBuilder = EndPointBuilder(fields)
}
