package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.{Response => InternalResponse}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Response model class.
  */
@JSExportAll
case class Response(override private[amf] val _internal: InternalResponse) extends Message {

  @JSExportTopLevel("model.domain.Response")
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

  /** Adds one Payload]to the payloads property of this Response]with the given media type and returns it for population. */
  def withPayload(mediaType: String): Payload = _internal.withPayload(Some(mediaType))

  override def linkCopy(): Response = _internal.linkCopy()
}
