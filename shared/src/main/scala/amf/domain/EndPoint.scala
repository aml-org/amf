package amf.domain

import amf.common.AMFAST
import amf.domain.Annotation.ParentEndPoint
import amf.metadata.domain.EndPointModel._

/**
  * EndPoint internal model
  */
case class EndPoint(fields: Fields, annotations: Annotations) extends DomainElement {

  val name: String               = fields(Name)
  val description: String        = fields(Description)
  val path: String               = fields(Path)
  val operations: Seq[Operation] = fields(Operations)
  val parameters: Seq[Parameter] = fields(Parameters)

  val parent: Option[EndPoint] =
    annotations.find(_.isInstanceOf[ParentEndPoint]).map(_.asInstanceOf[ParentEndPoint]).map(_.parent)

  val relativePath: String = parent.map(p => path.stripPrefix(p.path)).getOrElse(path)

  def withName(name: String): this.type                     = set(Name, name)
  def withDescription(description: String): this.type       = set(Description, description)
  def withPath(path: String): this.type                     = set(Path, path)
  def withOperations(operations: Seq[Operation]): this.type = set(Operations, operations)
  def withParameters(parameters: Seq[Parameter]): this.type = set(Parameters, parameters)
}

object EndPoint {
  def apply(fields: Fields = Fields(), annotations: Annotations = new Annotations()): EndPoint =
    new EndPoint(fields, annotations)

  def apply(ast: AMFAST): EndPoint = new EndPoint(Fields(), Annotations(ast))
}
