package amf.builder

import amf.domain.{Fields, Parameter, Payload, Response}
import amf.metadata.domain.ResponseModel._

/**
  * Response builder.
  */
class ResponseBuilder extends Builder {

  override type T = Response

  def withName(name: String): ResponseBuilder = set(Name, name)

  def withDescription(description: String): ResponseBuilder = set(Description, description)

  def withStatusCode(statusCode: String): ResponseBuilder = set(StatusCode, statusCode)

  def withHeaders(headers: Seq[Parameter]): ResponseBuilder = set(Headers, headers)

  def withPayloads(payloads: Seq[Payload]): ResponseBuilder = set(Payloads, payloads)

  override def build: Response = Response(fields)
}

object ResponseBuilder {
  def apply(): ResponseBuilder = new ResponseBuilder()

  def apply(fields: Fields): ResponseBuilder = apply().copy(fields)
}
