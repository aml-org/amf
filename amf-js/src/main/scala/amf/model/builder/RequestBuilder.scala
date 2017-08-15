package amf.model.builder

import amf.model.{Parameter, Payload, Request}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Request domain element builder.
  */
@JSExportAll
case class RequestBuilder(private[amf] val internalBuilder: amf.builder.RequestBuilder = amf.builder.RequestBuilder())
    extends Builder {

  def withQueryParameters(queryParameters: js.Iterable[Parameter]): RequestBuilder = {
    internalBuilder.withQueryParameters(queryParameters.toList.map(_.element))
    this
  }

  def withHeaders(headers: js.Iterable[Parameter]): RequestBuilder = {
    internalBuilder.withHeaders(headers.toList.map(_.element))
    this
  }

  def withPayloads(payloads: js.Iterable[Payload]): RequestBuilder = {
    internalBuilder.withPayloads(payloads.toList.map(_.element))
    this
  }

  def build: Request = Request(internalBuilder.build)
}
