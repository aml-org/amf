package amf.plugins.domain.webapi.models

import amf.core.metamodel.{Field, Obj}
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement}
import amf.core.model.{BoolField, StrField}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.metamodel.OperationModel.{Request => OperationRequest, _}
import amf.plugins.domain.webapi.models.bindings.OperationBindings
import amf.plugins.domain.webapi.models.security.SecurityRequirement
import amf.plugins.domain.webapi.models.templates.ParametrizedTrait

/**
  * Operation internal model.
  */
case class Operation(fields: Fields, annotations: Annotations)
    extends NamedDomainElement
    with ExtensibleWebApiDomainElement
    with ServerContainer
    with DocumentedElement
    with Linkable {

  def method: StrField      = fields.field(Method)
  def description: StrField = fields.field(Description)
  def deprecated: BoolField = fields.field(Deprecated)
  def summary: StrField     = fields.field(Summary)
  // TODO: should return Option has field can be null
  def documentation: CreativeWork        = fields.field(Documentation)
  def schemes: Seq[StrField]             = fields.field(Schemes)
  def accepts: Seq[StrField]             = fields.field(Accepts)
  def contentType: Seq[StrField]         = fields.field(ContentType)
  def request: Request                   = requests.headOption.orNull
  def requests: Seq[Request]             = fields.field(OperationRequest)
  def responses: Seq[Response]           = fields.field(Responses)
  def security: Seq[SecurityRequirement] = fields.field(Security)
  def tags: Seq[Tag]                     = fields.field(Tags)
  def callbacks: Seq[Callback]           = fields.field(Callbacks)
  def servers: Seq[Server]               = fields.field(Servers)
  def isAbstract: BoolField              = fields.field(IsAbstract)
  def bindings: OperationBindings        = fields.field(Bindings)
  def operationId: StrField              = fields.field(OperationId)

  override def documentations: Seq[CreativeWork] = Seq(documentation)

  def traits: Seq[ParametrizedTrait] = extend collect { case t: ParametrizedTrait => t }

  def withMethod(method: String): this.type                     = set(Method, method)
  def withDescription(description: String): this.type           = set(Description, description)
  def withDeprecated(deprecated: Boolean): this.type            = set(Deprecated, deprecated)
  def withSummary(summary: String): this.type                   = set(Summary, summary)
  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)
  def withSchemes(schemes: Seq[String]): this.type              = set(Schemes, schemes.toList)
  def withAccepts(accepts: Seq[String]): this.type              = set(Accepts, accepts.toList)
  def withContentType(contentType: Seq[String]): this.type      = set(ContentType, contentType.toList)
  def withRequest(request: Request, annotations: Annotations = Annotations()): this.type =
    setArray(OperationRequest, Seq(request), annotations)
  def withResponses(responses: Seq[Response]): this.type          = setArray(Responses, responses)
  def withSecurity(security: Seq[SecurityRequirement]): this.type = setArray(Security, security)
  def withTags(tags: Seq[Tag]): this.type                         = setArray(Tags, tags)
  def withCallbacks(callbacks: Seq[Callback], annotations: Annotations = Annotations()): this.type =
    setArray(Callbacks, callbacks, annotations)
  def withServers(servers: Seq[Server]): this.type         = setArray(Servers, servers)
  def withAbstract(abs: Boolean): this.type                = set(IsAbstract, abs)
  def withBindings(bindings: OperationBindings): this.type = set(Bindings, bindings)
  def withOperationId(operationId: String): this.type      = set(OperationId, operationId)

  override def removeServers(): Unit = fields.removeField(OperationModel.Servers)
  def removeName(): fields.type      = fields.removeField(OperationModel.Name)

  def withResponse(name: String): Response = {
    val result = Response().withName(name).withStatusCode(if (name == "default") "200" else name)
    add(Responses, result)
    result
  }

  def withRequest(): Request = {
    val request = Request()
    setArray(OperationRequest, Seq(request))
    request
  }

  def withSecurity(name: String): SecurityRequirement = {
    val result = SecurityRequirement().withName(name)
    add(Security, result)
    result
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

  override def meta: Obj = OperationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + method.option().getOrElse("default-operation").urlComponentEncoded
  override def nameField: Field    = Name

  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Operation.apply
}

object Operation {

  def apply(): Operation = apply(Annotations())

  def apply(annotations: Annotations): Operation = new Operation(Fields(), annotations)
}
