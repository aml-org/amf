package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.apicontract.client.scala.model.domain.{Response => InternalResponse}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.shapes.client.platform.model.domain.operations.AbstractResponse

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Response model class.
  */
@JSExportAll
case class Response(override private[amf] val _internal: InternalResponse) extends Message(_internal) with AbstractResponse {

  @JSExportTopLevel("Response")
  def this() = this(InternalResponse())

  def statusCode: StrField             = _internal.statusCode
  def headers: ClientList[Parameter]   = _internal.headers.asClient
  def links: ClientList[TemplatedLink] = _internal.links.asClient

  /** Set statusCode property of this Response] */
  def withStatusCode(statusCode: String): this.type = {
    _internal.withStatusCode(statusCode)
    this
  }

  /** Set headers property of this Response] */
  def withHeaders(headers: ClientList[Parameter]): this.type = {
    _internal.withHeaders(headers.asInternal)
    this
  }

  def withLinks(links: ClientList[TemplatedLink]): this.type = {
    _internal.withLinks(links.asInternal)
    this
  }

  /**
    * Adds one Parameter to the headers property of this Response]and returns it for population.
    * Name property of the parameter is required.
    */
  def withHeader(name: String): Parameter = _internal.withHeader(name)

  override def linkCopy(): Response = _internal.linkCopy()

  override type PayloadType = Payload

  override def payload: Payload = _internal.payload

  override def withPayload(payload: PayloadType): this.type = {
    _internal.withPayload(payload)
    this
  }
}
