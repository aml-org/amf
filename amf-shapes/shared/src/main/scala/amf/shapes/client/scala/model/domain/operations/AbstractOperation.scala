package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.AbstractOperationModel
import amf.shapes.internal.domain.metamodel.operations.AbstractOperationModel._

abstract class AbstractOperation(fields: Fields, annotations: Annotations) extends NamedDomainElement {
  type RequestType <: AbstractRequest
  type ResponseType <: AbstractResponse

  def method: StrField             = fields.field(Method)
  def description: StrField        = fields.field(Description)
  def request: RequestType         = fields.field(Request)
  def responses: Seq[ResponseType] = fields.field(Responses)

  def withMethod(method: String): this.type                  = set(Method, method)
  def withDescription(description: String): this.type        = set(Description, description)
  def withRequest(request: RequestType): this.type           = set(Request, request)
  def withResponses(responses: Seq[ResponseType]): this.type = setArray(Responses, responses)

  private[amf] def buildResponse: ResponseType

  def withResponse(name: String = "default"): ResponseType = {
    val result = buildResponse.withName(name)
    val prev = Option(responses).getOrElse(Nil)
    withResponses(prev :+ result)
    result
  }

  private[amf] def buildRequest: RequestType

  def withRequest(name: String = "default"): RequestType = {
    val result = buildRequest.withName(name)
    withRequest(result)
    result
  }

  override def meta: AbstractOperationModel = AbstractOperationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = {
    "/" + name.value()
  }
  override def nameField: Field = Name

}
