package amf.plugins.domain.webapi.models

import amf.core.metamodel.{Field, Obj}
import amf.core.model.{BoolField, StrField}
import amf.core.model.domain.{DomainElement, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.metamodel.OperationModel.{Request => OperationRequest, _}
import amf.plugins.domain.webapi.models.security.ParametrizedSecurityScheme
import amf.plugins.domain.webapi.models.templates.ParametrizedTrait
import amf.core.utils.AmfStrings

/**
  * Operation internal model.
  */
case class Operation(fields: Fields, annotations: Annotations)
    extends NamedDomainElement
    with ExtensibleWebApiDomainElement
    with ServerContainer {

  def method: StrField                          = fields.field(Method)
  def description: StrField                     = fields.field(Description)
  def deprecated: BoolField                     = fields.field(Deprecated)
  def summary: StrField                         = fields.field(Summary)
  def documentation: CreativeWork               = fields.field(Documentation)
  def schemes: Seq[StrField]                    = fields.field(Schemes)
  def accepts: Seq[StrField]                    = fields.field(Accepts)
  def contentType: Seq[StrField]                = fields.field(ContentType)
  def request: Request                          = fields.field(OperationRequest)
  def responses: Seq[Response]                  = fields.field(Responses)
  def security: Seq[ParametrizedSecurityScheme] = fields.field(Security)
  def tags: Seq[String]                         = fields(Tags)
  def callbacks: Seq[Callback]                  = fields.field(Callbacks)
  def servers: Seq[Server]                      = fields.field(Servers)

  def traits: Seq[ParametrizedTrait] = extend collect { case t: ParametrizedTrait => t }

  def withMethod(method: String): this.type                     = set(Method, method)
  def withDescription(description: String): this.type           = set(Description, description)
  def withDeprecated(deprecated: Boolean): this.type            = set(Deprecated, deprecated)
  def withSummary(summary: String): this.type                   = set(Summary, summary)
  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)
  def withSchemes(schemes: Seq[String]): this.type              = set(Schemes, schemes.toList)
  def withAccepts(accepts: Seq[String]): this.type              = set(Accepts, accepts.toList)
  def withContentType(contentType: Seq[String]): this.type      = set(ContentType, contentType.toList)
  def withRequest(request: Request): this.type                  = set(OperationRequest, request)
  def withResponses(responses: Seq[Response]): this.type        = setArray(Responses, responses)
  def withSecurity(security: Seq[DomainElement]): this.type     = setArray(Security, security)
  def withTags(tag: Seq[String]): this.type                     = set(Tags, tag.toList)
  def withCallbacks(callbacks: Seq[Callback]): this.type        = setArray(Callbacks, callbacks)
  def withServers(servers: Seq[Server]): this.type              = setArray(Servers, servers)

  override def removeServers(): Unit = fields.removeField(OperationModel.Servers)

  def withResponse(name: String): Response = {
    val result = Response().withName(name).withStatusCode(if (name == "default") "200" else name)
    add(Responses, result)
    result
  }

  def withRequest(): Request = {
    val request = Request()
    set(OperationRequest, request)
    request
  }

  def withSecurity(name: String): ParametrizedSecurityScheme = {
    val result = ParametrizedSecurityScheme().withName(name)
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

  override def meta: Obj = OperationModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String        = "/" + method.option().getOrElse("default-operation").urlComponentEncoded
  override protected def nameField: Field = Name
}

object Operation {

  def apply(): Operation = apply(Annotations())

  def apply(annotations: Annotations): Operation = new Operation(Fields(), annotations)
}
