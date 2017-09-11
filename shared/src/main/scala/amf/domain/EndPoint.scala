package amf.domain

import amf.common.AMFAST
import amf.common.core.Strings
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
  def parameters: Seq[Parameter] = fields(UriParameters)

  def parent: Option[EndPoint] = annotations.find(classOf[ParentEndPoint]).map(_.parent)

  def relativePath: String = parent.map(p => path.stripPrefix(p.path)).getOrElse(path)

  def withName(name: String): this.type                     = set(Name, name)
  def withDescription(description: String): this.type       = set(Description, description)
  def withPath(path: String): this.type                     = set(Path, path)
  def withOperations(operations: Seq[Operation]): this.type = setArray(Operations, operations)
  def withParameters(parameters: Seq[Parameter]): this.type = setArray(UriParameters, parameters)

  def withOperation(method: String): Operation = {
    val result = Operation().withMethod(method)
    add(Operations, result)
    result
  }

  def withParameter(name: String): Parameter = {
    val result = Parameter().withName(name)
    add(UriParameters, result)
    result
  }

  override def adopted(parent: String): this.type = withId(parent + "/end-points/" + path.urlEncoded)
}

object EndPoint {

  def apply(): EndPoint = apply(Annotations())

  def apply(ast: AMFAST): EndPoint = apply(Annotations(ast))

  def apply(annotations: Annotations): EndPoint = EndPoint(Fields(), annotations)
}
