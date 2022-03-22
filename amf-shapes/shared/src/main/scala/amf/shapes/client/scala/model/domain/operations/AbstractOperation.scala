package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractOperationModel
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractOperationModel.{
  Description,
  Method,
  Name,
  Request,
  Response
}

abstract class AbstractOperation(fields: Fields, annotations: Annotations) extends NamedDomainElement {

  def method: StrField           = fields.field(Method)
  def description: StrField      = fields.field(Description)
  def request: AbstractRequest   = fields.field(Request)
  def response: AbstractResponse = fields.field(Response)

  def withMethod(method: String): this.type               = set(Method, method)
  def withDescription(description: String): this.type     = set(Description, description)
  def withRequest(request: AbstractRequest): this.type    = set(Request, request)
  def withResponse(response: AbstractResponse): this.type = set(Response, response)

  protected def buildResponse: AbstractResponse

  def withResponse(name: String = "default"): AbstractResponse = {
    val result = buildResponse.withName(name)
    withResponse(result)
    result
  }

  protected def buildRequest: AbstractRequest

  def withRequest(name: String = "default"): AbstractRequest = {
    val result = buildRequest.withName(name)
    withRequest(result)
    result
  }

  override def meta: DomainElementModel = AbstractOperationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = {
    "/" + name.value()
  }
  override def nameField: Field = Name

}
