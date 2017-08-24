package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * request js class
  */
@JSExportAll
case class Request private[model] (private val request: amf.domain.Request) extends DomainElement {

  def this() = this(amf.domain.Request())

  val queryParameters: js.Iterable[Parameter] = request.queryParameters.map(Parameter).toJSArray
  val headers: js.Iterable[Parameter]         = request.headers.map(Parameter).toJSArray
  val payloads: js.Iterable[Payload]          = request.payloads.map(Payload).toJSArray

  override def equals(other: Any): Boolean = other match {
    case that: Request =>
      (that canEqual this) &&
        request == that.request
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Request]

  override private[amf] def element: amf.domain.Request = request

  def withQueryParameters(parameters: js.Iterable[Parameter]): this.type = {
    request.withQueryParameters(parameters.toSeq.map(_.element))
    this
  }
  def withHeaders(headers: js.Iterable[Parameter]): this.type = {
    request.withHeaders(headers.toSeq.map(_.element))
    this
  }
  def withPayloads(payloads: js.Iterable[Payload]): this.type = {
    request.withPayloads(payloads.toSeq.map(_.element))
    this
  }

  def withQueryParameter(name: String): Parameter = Parameter(request.withQueryParameter(name))

  def withHeader(name: String): Parameter = Parameter(request.withHeader(name))

  def withPayload(): Payload = Payload(request.withPayload())
}
