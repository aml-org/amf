package amf.apicontract.client.platform.model.domain

import amf.core.client.platform.model.BoolField
import amf.core.client.platform.model.domain.Shape
import amf.apicontract.client.scala.model.domain.{Request => InternalRequest}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Request model class.
  */
@JSExportAll
case class Request(override private[amf] val _internal: InternalRequest) extends Message(_internal) {

  @JSExportTopLevel("model.domain.Request")
  def this() = this(InternalRequest())

  def required: BoolField                     = _internal.required
  def queryParameters: ClientList[Parameter]  = _internal.queryParameters.asClient
  def headers: ClientList[Parameter]          = _internal.headers.asClient
  def queryString: Shape                      = _internal.queryString
  def uriParameters: ClientList[Parameter]    = _internal.uriParameters.asClient
  def cookieParameters: ClientList[Parameter] = _internal.cookieParameters.asClient

  /** Set required property of this Request. */
  def withRequired(required: Boolean): this.type = {
    _internal.withRequired(required)
    this
  }

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

  /** Set query string property of this Request. */
  def withQueryString(queryString: Shape): this.type = {
    _internal.withQueryString(queryString)
    this
  }

  /** Set uriParameters property of this Request. */
  def withUriParameters(uriParameters: ClientList[Parameter]): this.type = {
    _internal.withUriParameters(uriParameters.asInternal)
    this
  }

  /** Set cookieParameters property of this Request. */
  def withCookieParameters(cookieParameters: ClientList[Parameter]): this.type = {
    _internal.withCookieParameters(cookieParameters.asInternal)
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

  /**
    * Adds one Parameter to the uriParameters property of this Request and returns it for population.
    * Name property of the parameter is required.
    */
  def withUriParameter(name: String): Parameter = _internal.withUriParameter(name)

  /**
    * Adds one Parameter to the cookieParameters property of this Request and returns it for population.
    * Name property of the parameter is required.
    */
  def withCookieParameter(name: String): Parameter = _internal.withCookieParameter(name)

  override def linkCopy(): Request = _internal.linkCopy()
}
