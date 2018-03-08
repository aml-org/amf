package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.plugins.domain.webapi.models.{Request => InternalRequest}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Request model class.
  */
@JSExportAll
case class Request(override private[amf] val _internal: InternalRequest) extends DomainElement {

  @JSExportTopLevel("Request")
  def this() = this(InternalRequest())

  def queryParameters: ClientList[Parameter] = _internal.queryParameters.asClient
  def headers: ClientList[Parameter]         = _internal.headers.asClient
  def payloads: ClientList[Payload]          = _internal.payloads.asClient
  def queryString: Shape                     = _internal.queryString

  /** Set queryParameters property of this Request. */
  def withQueryParameters(parameters: ClientList[Parameter]): this.type = {
    _internal.withQueryParameters(parameters.asInternal)
    this
  }

  /** Set headers property of this Request. */
  def withHeaders(headers: ClientList[Parameter]): this.type = {
    _internal.withHeaders(headers.asInternal)
    this
  }

  /** Set payloads property of this Request. */
  def withPayloads(payloads: ClientList[Payload]): this.type = {
    _internal.withPayloads(payloads.asInternal)
    this
  }

  /**
    * Adds one Parameter to the queryParameters property of this Request and returns it for population.
    * Name property of the parameter is required.
    */
  def withQueryParameter(name: String): Parameter = _internal.withQueryParameter(name)

  /**
    * Adds one Parameter]to the headers property of this Request and returns it for population.
    * Name property of the parameter is required.
    */
  def withHeader(name: String): Parameter = _internal.withHeader(name)

  /** Adds one Payload to the payloads property of this Request and returns it for population. */
  def withPayload(): Payload = _internal.withPayload()

  /** Adds one Payload]to the payloads property of this Request with the given media type and returns it for population. */
  def withPayload(mediaType: String): Payload = _internal.withPayload(Some(mediaType))

  /** Set query string property of this Request. */
  def withQueryString(queryString: Shape): this.type = {
    _internal.withQueryString(queryString)
    this
  }
}
