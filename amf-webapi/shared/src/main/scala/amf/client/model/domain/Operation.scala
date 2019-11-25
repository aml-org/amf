package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.{BoolField, StrField}
import amf.plugins.domain.webapi.models.{Operation => InternalOperation}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Operation model class.
  */
@JSExportAll
case class Operation(override private[amf] val _internal: InternalOperation)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.Operation")
  def this() = this(InternalOperation())

  def method: StrField                          = _internal.method
  override def name: StrField                   = _internal.name
  def description: StrField                     = _internal.description
  def deprecated: BoolField                     = _internal.deprecated
  def summary: StrField                         = _internal.summary
  def documentation: CreativeWork               = _internal.documentation
  def schemes: ClientList[StrField]             = _internal.schemes.asClient
  def accepts: ClientList[StrField]             = _internal.accepts.asClient
  def contentType: ClientList[StrField]         = _internal.contentType.asClient
  def request: Request                          = _internal.request
  def responses: ClientList[Response]           = _internal.responses.asClient
  def security: ClientList[SecurityRequirement] = _internal.security.asClient
  def callbacks: ClientList[Callback]           = _internal.callbacks.asClient
  def servers: ClientList[Server]               = _internal.servers.asClient

  /** Set method property of this Operation. */
  def withMethod(method: String): this.type = {
    _internal.withMethod(method)
    this
  }

  /** Set name property of this Operation. */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set description property of this Operation. */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

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

  /** Set request property of this Operation. */
  def withRequest(request: Request): this.type = {
    _internal.withRequest(request)
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

  /**
    * Adds one Response to the responses property of this Operation and returns it for population.
    * Name property of the response is required.
    */
  def withResponse(name: String): Response = _internal.withResponse(name)

  /**
    * Adds a Request to this Operation and returns it for population.
    */
  def withRequest(): Request = _internal.withRequest()

  /**
    * Adds one Callback to the callbacks property of this Operation and returns it for population.
    * Name property of the callback is required.
    */
  def withCallback(name: String): Callback = _internal.withCallback(name)

  /**
    * Adds one Server to the servers property of this Operation and returns it for population.
    * Url property of the server is required.
    */
  def withServer(name: String): Server = _internal.withServer(name)
}
