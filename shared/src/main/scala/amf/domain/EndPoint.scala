package amf.domain

import amf.common.AMFAST
import amf.domain.Annotation.ParentEndPoint
import amf.metadata.domain.EndPointModel._

/**
  * EndPoint internal model
  */
case class EndPoint(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String               = fields(Name)
  def description: String        = fields(Description)
  def path: String               = fields(Path)
  def operations: Seq[Operation] = fields(Operations)
  def parameters: Seq[Parameter] = fields(Parameters)

  def parent: Option[EndPoint] = annotations.find(classOf[ParentEndPoint]).map(_.parent)

  def relativePath: String = parent.map(p => path.stripPrefix(p.path)).getOrElse(path)

  def withName(name: String): this.type                     = set(Name, name)
  def withDescription(description: String): this.type       = set(Description, description)
  def withPath(path: String): this.type                     = set(Path, path)
  def withOperations(operations: Seq[Operation]): this.type = setArray(Operations, operations)
  def withParameters(parameters: Seq[Parameter]): this.type = setArray(Parameters, parameters)
}

object EndPoint {
  def apply(): EndPoint = new EndPoint(Fields(), Annotations())

  def apply(ast: AMFAST): EndPoint = new EndPoint(Fields(), Annotations(ast))
}
