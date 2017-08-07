package amf.model

import amf.model.builder.ResponseBuilder

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * response js class
  */
@JSExportAll
case class Response private[model] (private[amf] val response: amf.domain.Response) extends DomainElement {

  val name: String = response.name

  val description: String = response.description

  val statusCode: String = response.statusCode

  val headers: js.Iterable[Parameter] = response.headers.map(Parameter).toJSArray

  val payloads: js.Iterable[Payload] = response.payloads.map(Payload).toJSArray

  def toBuilder: ResponseBuilder = ResponseBuilder(response.toBuilder)
}
