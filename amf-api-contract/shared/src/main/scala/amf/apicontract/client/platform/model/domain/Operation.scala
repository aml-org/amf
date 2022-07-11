package amf.apicontract.client.platform.model.domain

import amf.apicontract.client.platform.model.domain.bindings.OperationBindings
import amf.apicontract.client.platform.model.domain.federation.OperationFederationMetadata
import amf.apicontract.client.platform.model.domain.security.SecurityRequirement
import amf.apicontract.client.scala.model.domain.{Operation => InternalOperation}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.Linkable
import amf.core.client.platform.model.{BoolField, StrField}
import amf.shapes.client.platform.model.domain.CreativeWork
import amf.shapes.client.platform.model.domain.operations.AbstractOperation

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** Operation model class.
  */
@JSExportAll
case class Operation(override private[amf] val _internal: InternalOperation)
    extends AbstractOperation(_internal)
    with Linkable {

  override type RequestType  = Request
  override type ResponseType = Response

  override def request: RequestType = _internal.request

  override def response: ResponseType = _internal.responses.head

  override def responses: ClientList[Response] = _internal.responses.asClient

  override private[amf] def buildResponse = _internal.buildResponse

  override private[amf] def buildRequest = _internal.buildRequest

  override def withRequest(request: RequestType): this.type = {
    _internal.withRequest(request)
    this
  }

  @JSExportTopLevel("Operation")
  def this() = this(InternalOperation())

  def deprecated: BoolField                           = _internal.deprecated
  def summary: StrField                               = _internal.summary
  def documentation: CreativeWork                     = _internal.documentation
  def schemes: ClientList[StrField]                   = _internal.schemes.asClient
  def accepts: ClientList[StrField]                   = _internal.accepts.asClient
  def contentType: ClientList[StrField]               = _internal.contentType.asClient
  def requests: ClientList[Request]                   = _internal.requests.asClient
  def security: ClientList[SecurityRequirement]       = _internal.security.asClient
  def tags: ClientList[Tag]                           = _internal.tags.asClient
  def callbacks: ClientList[Callback]                 = _internal.callbacks.asClient
  def servers: ClientList[Server]                     = _internal.servers.asClient
  def isAbstract: BoolField                           = _internal.isAbstract
  def bindings: OperationBindings                     = _internal.bindings
  def operationId: StrField                           = _internal.operationId
  def federationMetadata: OperationFederationMetadata = _internal.federationMetadata

  /** Set deprecated property of this Operation. */
  def withDeprecated(deprecated: Boolean): this.type = {
    _internal.withDeprecated(deprecated)
    this
  }

  /** Set summary property of this Operation. */
  def withSummary(summary: String): this.type = {
    _internal.withSummary(summary)
    this
  }

  /** Set documentation property of this Operation using a CreativeWork. */
  def withDocumentation(documentation: CreativeWork): this.type = {
    _internal.withDocumentation(documentation)
    this
  }

  /** Set schemes property of this Operation. */
  def withSchemes(schemes: ClientList[String]): this.type = {
    _internal.withSchemes(schemes.asInternal)
    this
  }

  /** Set accepts property of this Operation. */
  def withAccepts(accepts: ClientList[String]): this.type = {
    _internal.withAccepts(accepts.asInternal)
    this
  }

  /** Set contentType property of this Operation. */
  def withContentType(contentType: ClientList[String]): this.type = {
    _internal.withContentType(contentType.asInternal)
    this
  }

  /** Set requests property of this Operation. */
  def withRequests(requests: ClientList[Request]): this.type = {
    _internal.withRequests(requests.asInternal)
    this
  }

  /** Set responses property of this Operation. */
  def withResponses(responses: ClientList[Response]): this.type = {
    _internal.withResponses(responses.asInternal)
    this
  }

  /** Set security property of this Operation. */
  def withSecurity(security: ClientList[SecurityRequirement]): this.type = {
    _internal.withSecurity(security.asInternal)
    this
  }

  /** Set callbacks property of this Operation. */
  def withCallbacks(callbacks: ClientList[Callback]): this.type = {
    _internal.withCallbacks(callbacks.asInternal)
    this
  }

  /** Set servers property of this Operation. */
  def withServers(servers: ClientList[Server]): this.type = {
    _internal.withServers(servers.asInternal)
    this
  }

  /** Set tags property of this Operation. */
  def withTags(tags: ClientList[Tag]): this.type = {
    _internal.withTags(tags.asInternal)
    this
  }

  /** Set abstract property of this Operation. */
  def withAbstract(abs: Boolean): this.type = {
    _internal.withAbstract(abs)
    this
  }

  /** Adds a Request to this Operation and returns it for population.
    */
  def withRequest(): Request = _internal.withRequest()

  /** Adds one Callback to the callbacks property of this Operation and returns it for population. Name property of the
    * callback is required.
    */
  def withCallback(name: String): Callback = _internal.withCallback(name)

  /** Adds one Server to the servers property of this Operation and returns it for population. Url property of the
    * server is required.
    */
  def withServer(name: String): Server = _internal.withServer(name)

  def withBindings(bindings: OperationBindings): this.type = {
    _internal.withBindings(bindings)
    this
  }

  def withOperationId(operationId: String): this.type = {
    _internal.withOperationId(operationId)
    this
  }

  def withFederationMetadata(federationMetadata: OperationFederationMetadata): this.type = {
    _internal.withFederationMetadata(federationMetadata)
    this
  }

  override def linkCopy(): Operation = _internal.linkCopy()

  override def withResponse(name: String): Response = { _internal.withResponse(name) }
}
