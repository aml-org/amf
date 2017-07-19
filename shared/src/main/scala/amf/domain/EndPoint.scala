package amf.domain

import amf.builder.EndPointBuilder
import amf.domain.Annotation.ParentEndPoint
import amf.metadata.domain.EndPointModel._

/**
  * EndPoint internal model
  */
case class EndPoint(override val fields: Fields) extends FieldHolder(fields) with DomainElement {

  override type This = EndPoint

  val name: String               = fields get Name
  val description: String        = fields get Description
  val path: String               = fields get Path
  val operations: Seq[Operation] = fields get Operations

  def simplePath: String = {
    val parent: Option[ParentEndPoint] = fields.getAnnotation(Path, classOf[ParentEndPoint])
    parent.map(p => path.stripPrefix(p.parent.path)).getOrElse(path)
  }

  def parent: Option[EndPoint] = {
    val parent: Option[ParentEndPoint] = fields.getAnnotation(Path, classOf[ParentEndPoint])
    parent.map(_.parent)
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[EndPoint]

  override def equals(other: Any): Boolean = other match {
    case that: EndPoint =>
      (that canEqual this) &&
        name == that.name &&
        description == that.description &&
        path == that.path &&
        operations == that.operations
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name, description, path, operations)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"EndPoint($name, $description, $path, $operations)"

  override def toBuilder: EndPointBuilder = EndPointBuilder(fields)
}
