package amf.builder

import amf.domain.{Fields, Parameter, Payload, Request}
import amf.metadata.domain.RequestModel._

/**
  * Request domain element builder.
  */
class RequestBuilder extends Builder {
  override type T = Request

  def withQueryParameters(queryParameters: Seq[Parameter]): this.type = set(QueryParameters, queryParameters)
  def withHeaders(headers: Seq[Parameter]): this.type                 = set(Headers, headers)
  def withPayloads(payloads: Seq[Payload]): this.type                 = set(Payloads, payloads)

  override def build: Request = Request(fields)
}

object RequestBuilder {
  def apply(): RequestBuilder = new RequestBuilder()

  def apply(fields: Fields): RequestBuilder = apply().copy(fields)
}
