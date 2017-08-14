package amf.domain

import amf.builder.EndPointBuilder
import amf.domain.Annotation.ParentEndPoint
import amf.metadata.domain.EndPointModel._

/**
  * EndPoint internal model
  */
case class EndPoint(fields: Fields, annotations: List[Annotation]) extends DomainElement {

  override type T = EndPoint

  val name: String               = fields(Name)
  val description: String        = fields(Description)
  val path: String               = fields(Path)
  val operations: Seq[Operation] = fields(Operations)
  val parameters: Seq[Parameter] = fields(Parameters)

  val parent: Option[EndPoint] =
    annotations.find(_.isInstanceOf[ParentEndPoint]).map(_.asInstanceOf[ParentEndPoint]).map(_.parent)

  val relativePath: String = parent.map(p => path.stripPrefix(p.path)).getOrElse(path)

  def canEqual(other: Any): Boolean = other.isInstanceOf[EndPoint]

  override def equals(other: Any): Boolean = other match {
    case that: EndPoint =>
      (that canEqual this) &&
        name == that.name &&
        description == that.description &&
        path == that.path &&
        operations == that.operations &&
        parameters == that.parameters
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name, description, path, operations, parameters)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"EndPoint($name, $description, $path, $operations, $parameters)"

  override def toBuilder: EndPointBuilder = EndPointBuilder(fields, annotations)
}
