package amf.builder

import amf.domain._
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

  override def build: Response = Response(fields, annotations)
}

object ResponseBuilder {
  def apply(): ResponseBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): ResponseBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): ResponseBuilder = new ResponseBuilder().withAnnotations(annotations)
}
