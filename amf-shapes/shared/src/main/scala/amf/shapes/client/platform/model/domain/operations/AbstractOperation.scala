package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.operations.{AbstractOperation => InternalAbstractOperation}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
abstract class AbstractOperation(override private[amf] val _internal: InternalAbstractOperation)
    extends DomainElement
    with NamedDomainElement
    with PlatformSecrets {

  type RequestType <: AbstractRequest
  type ResponseType <: AbstractResponse

  def method: StrField      = _internal.method
  def description: StrField = _internal.description

  def withResponse(name: String): ResponseType

  def withRequest(name: String): RequestType = {
    val result = buildRequest.withName(name)
    withRequest(result)
    result
  }

  // Cannot implement because RequestType and ResponseType are abstract
  def request: RequestType

  def response: ResponseType

  def responses: ClientList[ResponseType]

  private[amf] def buildResponse: ResponseType

  private[amf] def buildRequest: RequestType

  def withRequest(request: RequestType): this.type

  def withResponses(responses: ClientList[ResponseType]): this.type

  def withMethod(method: String): this.type = {
    _internal.withMethod(method)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
