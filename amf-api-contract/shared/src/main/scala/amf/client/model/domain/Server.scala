package amf.client.model.domain
import amf.client.convert.ApiClientConverters._

import amf.client.model.StrField
import amf.plugins.domain.apicontract.models.{Server => InternalServer}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Server model class.
  */
@JSExportAll
case class Server(override private[amf] val _internal: InternalServer) extends DomainElement {

  @JSExportTopLevel("model.domain.Server")
  def this() = this(InternalServer())

  def name: StrField                            = _internal.name
  def url: StrField                             = _internal.url
  def description: StrField                     = _internal.description
  def variables: ClientList[Parameter]          = _internal.variables.asClient
  def protocol: StrField                        = _internal.protocol
  def protocolVersion: StrField                 = _internal.protocolVersion
  def security: ClientList[SecurityRequirement] = _internal.security.asClient
  def bindings: ServerBindings                  = _internal.bindings

  /** Set url property of this Server. */
  def withUrl(url: String): this.type = {
    _internal.withUrl(url)
    this
  }

  /** Set description property of this Server. */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  /** Set host property of this Server. */
  def withVariables(variables: ClientList[Parameter]): this.type = {
    _internal.withVariables(variables.asInternal)
    this
  }

  def withProtocol(protocol: String): this.type = {
    _internal.withProtocol(protocol)
    this
  }

  def withProtocolVersion(protocolVersion: String): this.type = {
    _internal.withProtocolVersion(protocolVersion)
    this
  }

  def withSecurity(security: ClientList[SecurityRequirement]): this.type = {
    _internal.withSecurity(security.asInternal)
    this
  }

  def withBindings(bindings: ServerBindings): this.type = {
    _internal.withBindings(bindings)
    this
  }

  /**
    * Adds one Parameter to the variables property of this Server and returns it for population.
    * name property of the Parameter is required.
    */
  def withVariable(name: String): Parameter = _internal.withVariable(name)
}
