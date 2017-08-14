package amf.model.builder

import amf.model.{Parameter, Payload, Request}

import scala.collection.JavaConverters._

/**
  * Request domain element builder.
  */
case class RequestBuilder private (
    private val internalBuilder: amf.builder.RequestBuilder = amf.builder.RequestBuilder())
    extends Builder {

  def this() = this(amf.builder.RequestBuilder())

  def withQueryParameters(queryParameters: java.util.List[Parameter]): RequestBuilder = {
    internalBuilder.withQueryParameters(queryParameters.asScala.map(_.element))
    this
  }

  def withHeaders(headers: java.util.List[Parameter]): RequestBuilder = {
    internalBuilder.withHeaders(headers.asScala.map(_.element))
    this
  }

  def withPayloads(payloads: java.util.List[Payload]): RequestBuilder = {
    internalBuilder.withPayloads(payloads.asScala.map(_.element))
    this
  }

  def build: Request = Request(internalBuilder.build)
}
