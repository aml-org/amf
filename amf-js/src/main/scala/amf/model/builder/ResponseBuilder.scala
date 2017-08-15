package amf.model.builder

import amf.model.{Parameter, Payload, Response}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * Response builder.
  */
@JSExportAll
case class ResponseBuilder(
    private[amf] val internalBuilder: amf.builder.ResponseBuilder = amf.builder.ResponseBuilder())
    extends Builder {

  def withName(name: String): ResponseBuilder = {
    internalBuilder.withName(name)
    this
  }

  def withDescription(description: String): ResponseBuilder = {
    internalBuilder.withDescription(description)
    this
  }

  def withStatusCode(statusCode: String): ResponseBuilder = {
    internalBuilder.withStatusCode(statusCode)
    this
  }

  def withHeaders(headers: js.Iterable[Parameter]): ResponseBuilder = {
    internalBuilder.withHeaders(headers.toList.map(_.element))
    this
  }

  def withPayloads(payloads: js.Iterable[Payload]): ResponseBuilder = {
    internalBuilder.withPayloads(payloads.toList.map(_.element))
    this
  }

  def build: Response = Response(internalBuilder.build)
}
