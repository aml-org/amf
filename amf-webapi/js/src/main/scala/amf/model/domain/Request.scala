package amf.model.domain

import amf.plugins.domain.webapi.models

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS Request model class.
  */
@JSExportAll
case class Request private[model] (private val request: models.Request) extends DomainElement {

  @JSExportTopLevel("Request")
  def this() = this(models.Request())

  val queryParameters: js.Iterable[Parameter] = request.queryParameters.map(Parameter).toJSArray
  val headers: js.Iterable[Parameter]         = request.headers.map(Parameter).toJSArray
  val payloads: js.Iterable[Payload]          = request.payloads.map(Payload).toJSArray
  val queryString: Shape                      = Option(request.queryString).map(Shape(_)).orNull

  override private[amf] def element: models.Request = request

  /** Set queryParameters property of this [[Request]]. */
  def withQueryParameters(parameters: js.Iterable[Parameter]): this.type = {
    request.withQueryParameters(parameters.toSeq.map(_.element))
    this
  }

  /** Set headers property of this [[Request]]. */
  def withHeaders(headers: js.Iterable[Parameter]): this.type = {
    request.withHeaders(headers.toSeq.map(_.element))
    this
  }

  /** Set payloads property of this [[Request]]. */
  def withPayloads(payloads: js.Iterable[Payload]): this.type = {
    request.withPayloads(payloads.toSeq.map(_.element))
    this
  }

  /**
    * Adds one [[Parameter]] to the queryParameters property of this [[Request]] and returns it for population.
    * Name property of the parameter is required.
    */
  def withQueryParameter(name: String): Parameter = Parameter(request.withQueryParameter(name))

  /**
    * Adds one [[Parameter]] to the headers property of this [[Request]] and returns it for population.
    * Name property of the parameter is required.
    */
  def withHeader(name: String): Parameter = Parameter(request.withHeader(name))

  /** Adds one [[Payload]] to the payloads property of this [[Request]] and returns it for population. */
  def withPayload(): Payload = Payload(request.withPayload())

  /** Adds one [[Payload]] to the payloads property of this [[Request]] with the given media type and returns it for population. */
  def withPayload(mediaType: String): Payload = Payload(request.withPayload(Some(mediaType)))

  /** Set query string property of this [[Request]]. */
  def withQueryString(queryString: Shape): this.type = {
    request.withQueryString(queryString.shape)
    this
  }
}
