package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.StrField
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.ShapeOperationModel
import amf.shapes.internal.domain.metamodel.operations.ShapeOperationModel._
import org.yaml.model.YPart

case class ShapeOperation(fields: Fields, annotations: Annotations) extends AbstractOperation(fields, annotations) {
  override type RequestType  = ShapeRequest
  override type ResponseType = ShapeResponse

  override private[amf] def buildResponse: ShapeResponse = ShapeResponse()
  override private[amf] def buildRequest: ShapeRequest   = ShapeRequest()

  override def method: StrField              = fields.field(Method)
  override def request: ShapeRequest         = requests.head
  def requests: Seq[ShapeRequest]            = fields.field(Request)
  override def responses: Seq[ShapeResponse] = fields.field(Responses)

  override def withMethod(method: String): this.type                   = set(Method, method)
  override def withRequest(request: ShapeRequest): this.type           = setArray(Request, Seq(request))
  override def withResponses(responses: Seq[ShapeResponse]): this.type = setArray(Responses, responses)
  override def nameField: Field                                        = Name
  override def meta: ShapeOperationModel.type                          = ShapeOperationModel
}

object ShapeOperation {
  def apply(): ShapeOperation = apply(Annotations())

  def apply(ast: YPart): ShapeOperation = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapeOperation = new ShapeOperation(Fields(), annotations)
}
