package amf.model.builder

import amf.model.Payload

/**
  * Payload domain element builder.
  */
case class PayloadBuilder private (
    private val internalBuilder: amf.builder.PayloadBuilder = amf.builder.PayloadBuilder())
    extends Builder {

  def this() = this(amf.builder.PayloadBuilder())

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
