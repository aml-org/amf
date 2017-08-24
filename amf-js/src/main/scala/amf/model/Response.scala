package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * response js class
  */
@JSExportAll
case class Response private[model] (private val response: amf.domain.Response) extends DomainElement {

  def this() = this(amf.domain.Response())

  val name: String                    = response.name
  val description: String             = response.description
  val statusCode: String              = response.statusCode
  val headers: js.Iterable[Parameter] = response.headers.map(Parameter).toJSArray
  val payloads: js.Iterable[Payload]  = response.payloads.map(Payload).toJSArray

  override def equals(other: Any): Boolean = other match {
    case that: Response =>
      (that canEqual this) &&
        response == that.response
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Response]

  override private[amf] def element: amf.domain.Response = response

  def withName(name: String): this.type = {
    response.withName(name)
    this
  }
  def withDescription(description: String): this.type = {
    response.withDescription(description)
    this
  }
  def withStatusCode(statusCode: String): this.type = {
    response.withStatusCode(statusCode)
    this
  }
  def withHeaders(headers: js.Iterable[Parameter]): this.type = {
    response.withHeaders(headers.toSeq.map(_.element))
    this
  }
  def withPayloads(payloads: js.Iterable[Payload]): this.type = {
    response.withPayloads(payloads.toSeq.map(_.element))
    this
  }

  def withHeader(name: String): Parameter = Parameter(response.withHeader(name))

  def withPayload(): Payload = Payload(response.withPayload())
}
