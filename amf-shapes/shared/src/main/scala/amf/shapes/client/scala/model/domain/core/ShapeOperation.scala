package amf.shapes.client.scala.model.domain.core

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{CreativeWork, DocumentedElement}
import amf.shapes.internal.domain.metamodel.core.ShapeOperationModel
import amf.shapes.internal.domain.metamodel.core.ShapeOperationModel.{
  Accepts,
  ContentType,
  Description,
  Documentation,
  Method,
  OperationId,
  Request,
  Responses
}

private[amf] case class ShapeOperation(fields: Fields, annotations: Annotations)
    extends NamedDomainElement
    with DocumentedElement
    with Linkable {

  def method: StrField      = fields.field(Method)
  def description: StrField = fields.field(Description)
  // TODO: should return Option has field can be null
  def documentation: CreativeWork   = fields.field(Documentation)
  def accepts: Seq[StrField]        = fields.field(Accepts)
  def contentType: Seq[StrField]    = fields.field(ContentType)
  def request: ShapeRequest         = fields.field(Request)
  def responses: Seq[ShapeResponse] = fields.field(Responses)
  def operationId: StrField         = fields.field(OperationId)

  override def documentations: Seq[CreativeWork] = Seq(documentation)

  def withMethod(method: String): this.type           = set(Method, method)
  def withDescription(description: String): this.type = set(Description, description)

  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)
  def withAccepts(accepts: Seq[String]): this.type              = set(Accepts, accepts.toList)
  def withContentType(contentType: Seq[String]): this.type      = set(ContentType, contentType.toList)
  def withRequest(request: ShapeRequest): this.type =
    set(Request, request)
  def withResponses(responses: Seq[ShapeResponse]): this.type = setArray(Responses, responses)
  def withOperationId(operationId: String): this.type         = set(OperationId, operationId)

  def withResponse(name: String): ShapeResponse = {
    val result = ShapeResponse().withName(name).withStatusCode(if (name == "default") "200" else name)
    add(Responses, result)
    result
  }

  def withRequest(): ShapeRequest = {
    val request = ShapeRequest()
    setArray(Request, Seq(request))
    request
  }

  override def linkCopy(): ShapeOperation = ShapeOperation().withId(id)

  override def meta: ShapeOperationModel.type = ShapeOperationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = {
    val name = if (operationId.option().isDefined) {
      operationId.value().urlComponentEncoded
    } else {
      method.option().getOrElse("default-operation").urlComponentEncoded
    }
    "/" + name
  }
  override def nameField: Field = Method

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ShapeOperation.apply
}

object ShapeOperation {

  def apply(): ShapeOperation = apply(Annotations())

  def apply(annotations: Annotations): ShapeOperation = new ShapeOperation(Fields(), annotations)
}
