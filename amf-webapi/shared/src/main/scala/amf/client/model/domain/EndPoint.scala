package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.{EndPoint => InternalEndPoint}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * EndPoint model class.
  */
@JSExportAll
case class EndPoint(override private[amf] val _internal: InternalEndPoint)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.EndPoint")
  def this() = this(InternalEndPoint())

  override def name: StrField                   = _internal.name
  def description: StrField                     = _internal.description
  def summary: StrField                         = _internal.summary
  def path: StrField                            = _internal.path
  def operations: ClientList[Operation]         = _internal.operations.asClient
  def parameters: ClientList[Parameter]         = _internal.parameters.asClient
  def payloads: ClientList[Payload]             = _internal.payloads.asClient
  def servers: ClientList[Server]               = _internal.servers.asClient
  def security: ClientList[SecurityRequirement] = _internal.security.asClient

  def parent: ClientOption[EndPoint] = _internal.parent.asClient

  /** Get the part of the path property that was defined by this EndPoint. */
  def relativePath: String = _internal.relativePath

  /** Set name property of this EndPoint */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set description property of this EndPoint */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  /** Set summary property of this EndPoint */
  def withSummary(summary: String): this.type = {
    _internal.withSummary(summary)
    this
  }

  /** Set full path property of this EndPoint */
  def withPath(path: String): this.type = {
    _internal.withPath(path)
    this
  }

  /** Set operations property of this EndPoint */
  def withOperations(operations: ClientList[Operation]): this.type = {
    _internal.withOperations(operations.asInternal)
    this
  }

  /** Set parameters property of this EndPoint */
  def withParameters(parameters: ClientList[Parameter]): this.type = {
    _internal.withParameters(parameters.asInternal)
    this
  }

  /** Set payloads property of this EndPoint */
  def withPayloads(payloads: ClientList[Payload]): this.type = {
    _internal.withPayloads(payloads.asInternal)
    this
  }

  /** Set servers property of this EndPoint */
  def withServers(servers: ClientList[Server]): this.type = {
    _internal.withServers(servers.asInternal)
    this
  }

  /** Set security property of this EndPoint using a list of SecurityRequirement. */
  def withSecurity(security: ClientList[SecurityRequirement]): this.type = {
    _internal.withSecurity(security.asInternal)
    this
  }

  /**
    * Adds one Operation to the operations property of this EndPoint and returns it for population.
    * Method property of the operation is required.
    */
  def withOperation(method: String): Operation = _internal.withOperation(method)

  /**
    * Adds one Parameter to the uriParameters property of this EndPoint and returns it for population.
    * Name property of the parameter is required.
    */
  def withParameter(name: String): Parameter = _internal.withParameter(name)

  /**
    * Adds one Payload to the payloads property of this EndPoint and returns it for population.
    * mediaType property of the parameter is required.
    */
  def withPayload(name: String): Payload = _internal.withPayload(Some(name))

  /**
    * Adds one Server to the servers property of this EndPoint and returns it for population.
    * url property of the server is required.
    */
  def withServer(url: String): Server = _internal.withServer(url)
}
