package amf.model.builder

import amf.model.Payload

import scala.scalajs.js.annotation.JSExportAll

/**
  * Payload domain element builder.
  */
@JSExportAll
case class PayloadBuilder(private[amf] val internalBuilder: amf.builder.PayloadBuilder = amf.builder.PayloadBuilder())
    extends Builder {

  def withMediaType(mediaType: String): PayloadBuilder = {
    internalBuilder.withMediaType(mediaType)
    this
  }

  def withSchema(schema: String): PayloadBuilder = {
    internalBuilder.withSchema(schema)
    this
  }

  def build: Payload = Payload(internalBuilder.build)
}
