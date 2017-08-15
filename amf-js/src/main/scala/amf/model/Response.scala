package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * response js class
  */
@JSExportAll
case class Response private[model] (private val response: amf.domain.Response) extends DomainElement {

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
}
