package amf.apicontract.client.scala.model.domain

import amf.apicontract.client.scala.model.domain.bindings.OperationBindings
import amf.apicontract.client.scala.model.domain.federation.OperationFederationMetadata
import amf.apicontract.client.scala.model.domain.templates.ParametrizedTrait
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.metamodel.domain.OperationModel.{Request => OperationRequest, _}
import amf.core.client.scala.model.domain.{AmfArray, DomainElement, Linkable}
import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{CreativeWork, DocumentedElement}
import amf.shapes.client.scala.model.domain.operations.AbstractOperation

/** Operation internal model.
  */
case class Operation(fields: Fields, annotations: Annotations)
    extends AbstractOperation(fields, annotations)
    with SecuredElement
    with ExtensibleWebApiDomainElement
    with ServerContainer
    with DocumentedElement
    with Linkable {

  override type RequestType  = Request
  override type ResponseType = Response

  override private[amf] def buildResponse = Response()

  override private[amf] def buildRequest = Request()

  override def method: StrField = fields.field(Method)
  def deprecated: BoolField     = fields.field(Deprecated)
  def summary: StrField         = fields.field(Summary)
  // TODO: should return Option has field can be null
  def documentation: CreativeWork                     = fields.field(Documentation)
  def schemes: Seq[StrField]                          = fields.field(Schemes)
  def accepts: Seq[StrField]                          = fields.field(Accepts)
  def contentType: Seq[StrField]                      = fields.field(ContentType)
  override def request: Request                       = requests.headOption.orNull
  def requests: Seq[Request]                          = fields.field(OperationRequest)
  override def responses: Seq[Response]               = fields.field(Responses)
  def tags: Seq[Tag]                                  = fields.field(Tags)
  def callbacks: Seq[Callback]                        = fields.field(Callbacks)
  def servers: Seq[Server]                            = fields.field(Servers)
  def isAbstract: BoolField                           = fields.field(IsAbstract)
  def bindings: OperationBindings                     = fields.field(Bindings)
  def operationId: StrField                           = fields.field(OperationId)
  def federationMetadata: OperationFederationMetadata = fields.field(FederationMetadata)

  override def documentations: Seq[CreativeWork] = Seq(documentation)

  def traits: Seq[ParametrizedTrait] = extend collect { case t: ParametrizedTrait => t }

  override def withMethod(method: String): this.type              = set(Method, method)
  def withDeprecated(deprecated: Boolean): this.type              = set(Deprecated, deprecated)
  def withSummary(summary: String): this.type                     = set(Summary, summary)
  def withDocumentation(documentation: CreativeWork): this.type   = set(Documentation, documentation)
  def withSchemes(schemes: Seq[String]): this.type                = set(Schemes, schemes.toList)
  def withAccepts(accepts: Seq[String]): this.type                = set(Accepts, accepts.toList)
  def withContentType(contentType: Seq[String]): this.type        = set(ContentType, contentType.toList)
  override def withRequest(request: Request): this.type           = setArray(OperationRequest, Seq(request))
  def withRequests(request: Seq[Request]): this.type              = setArray(OperationRequest, request)
  override def withResponses(responses: Seq[Response]): this.type = setArray(Responses, responses)
  def withTags(tags: Seq[Tag]): this.type                         = setArray(Tags, tags)
  def withCallbacks(callbacks: Seq[Callback]): this.type          = setArray(Callbacks, callbacks)
  def withServers(servers: Seq[Server]): this.type                = setArray(Servers, servers)
  def withAbstract(abs: Boolean): this.type                       = set(IsAbstract, abs)
  def withBindings(bindings: OperationBindings): this.type        = set(Bindings, bindings)
  def withOperationId(operationId: String): this.type             = set(OperationId, operationId)
  def withFederationMetadata(federationMetadata: OperationFederationMetadata): this.type =
    set(FederationMetadata, federationMetadata)

  override def removeServers(): Unit = fields.removeField(OperationModel.Servers)
  def removeName(): fields.type      = fields.removeField(OperationModel.Name)

  override def withResponse(name: String): Response = {
    val result = Response().withName(name).withStatusCode(if (name == "default") "200" else name)
    add(Responses, result)
    result
  }

  def withRequest(): Request = {
    val request = Request()
    setArray(OperationRequest, Seq(request))
    request
  }

  def withInferredRequest(): Request = {
    val request = Request()
    fields.set(id, OperationRequest, AmfArray(Seq(request), Annotations.inferred()), Annotations.inferred())
    request
  }

  def withCallback(name: String): Callback = {
    val result = Callback().withName(name)
    add(Callbacks, result)
    result
  }

  def withServer(url: String): Server = {
    val result = Server().withUrl(url)
    add(Servers, result)
    result
  }

  override def linkCopy(): Operation = Operation().withId(id)

  override def meta: OperationModel.type = OperationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = {
    val methodName = method.option().getOrElse("default-operation").urlComponentEncoded
    val opId       = operationId.option().map(id => "/" + id.urlComponentEncoded).getOrElse("")
    "/" + methodName + opId
  }
  override def nameField: Field = Name

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Operation.apply
}

object Operation {

  def apply(): Operation = apply(Annotations())

  def apply(annotations: Annotations): Operation = new Operation(Fields(), annotations)
}
