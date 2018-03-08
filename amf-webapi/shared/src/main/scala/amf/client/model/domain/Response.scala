package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.{Response => InternalResponse}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Response model class.
  */
@JSExportAll
case class Response(override private[amf] val _internal: InternalResponse) extends DomainElement {

  @JSExportTopLevel("model.domain.Response")
  def this() = this(InternalResponse())

  def name: StrField                 = _internal.name
  def description: StrField          = _internal.description
  def statusCode: StrField           = _internal.statusCode
  def headers: ClientList[Parameter] = _internal.headers.asClient
  def payloads: ClientList[Payload]  = _internal.payloads.asClient
  def examples: ClientList[Example]  = _internal.examples.asClient

  /** Set name property of this Response. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set description property of this Response] */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

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

  /** Set payloads property of this Response] */
  def withPayloads(payloads: ClientList[Payload]): this.type = {
    _internal.withPayloads(payloads.asInternal)
    this
  }

  /** Set examples property of this Response] */
  def withExamples(examples: ClientList[Example]): this.type = {
    _internal.withExamples(examples.asInternal)
    this
  }

  /**
    * Adds one Parameter to the headers property of this Response]and returns it for population.
    * Name property of the parameter is required.
    */
  def withHeader(name: String): Parameter = _internal.withHeader(name)

  /** Adds one Payload to the payloads property of this Response]and returns it for population. */
  def withPayload(): Payload = _internal.withPayload()

  /** Adds one Payload]to the payloads property of this Response]with the given media type and returns it for population. */
  def withPayload(mediaType: String): Payload = _internal.withPayload(Some(mediaType))
}
