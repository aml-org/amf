package amf.model

import amf.model.builder.ResponseBuilder

import scala.collection.JavaConverters._

/**
  * response jvm class
  */
case class Response private[model] (private[amf] val response: amf.domain.Response) extends DomainElement {

  val name: String = response.name

  val description: String = response.description

  val statusCode: String = response.statusCode

  val headers: java.util.List[Parameter] = response.headers.map(Parameter).asJava

  val payloads: java.util.List[Payload] = response.payloads.map(Payload).asJava

  def toBuilder: ResponseBuilder = ResponseBuilder(response.toBuilder)

}
