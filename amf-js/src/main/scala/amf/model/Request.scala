package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * request js class
  */
@JSExportAll
case class Request private[model] (private val request: amf.domain.Request) extends DomainElement {

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
}
