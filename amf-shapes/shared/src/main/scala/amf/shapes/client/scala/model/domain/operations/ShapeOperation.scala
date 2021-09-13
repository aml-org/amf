package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.ShapeOperationModel
import amf.shapes.internal.domain.metamodel.operations.ShapeOperationModel._

case class ShapeOperation(fields: Fields, annotations: Annotations)
  extends NamedDomainElement {

  def method: StrField      = fields.field(Method)
  def description: StrField = fields.field(Description)
  def request: ShapeRequest  = fields.field(Request)
  def response: ShapeResponse  = fields.field(Response)

  def withMethod(method: String): this.type                     = set(Method, method)
  def withDescription(description: String): this.type           = set(Description, description)
  def withRequest(request: ShapeRequest): this.type             = set(Request, request)
  def withResponse(response: ShapeResponse): this.type           = set(Response, response)


  def withResponse(name: String = "default"): ShapeResponse = {
    val result = ShapeResponse().withName(name)
    withResponse(result)
    result
  }

  def withRequest(name: String = "default"): ShapeRequest = {
    val result = ShapeRequest().withName(name)
    withRequest(result)
    result
  }

  override def meta: ShapeOperationModel.type = ShapeOperationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = {
  "/" + name.value()
  }
  override def nameField: Field    = Name

}

  object ShapeOperation {

    def apply(): ShapeOperation = apply(Annotations())

    def apply(annotations: Annotations): ShapeOperation = new ShapeOperation(Fields(), annotations)
  }


