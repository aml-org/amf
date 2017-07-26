package amf.builder

import amf.domain.{Fields, Payload}
import amf.metadata.domain.PayloadModel._

/**
  * Payload domain element builder.
  */
class PayloadBuilder extends Builder {
  override type T = Payload

  def withMediaType(mediaType: String): PayloadBuilder = set(MediaType, mediaType)

  def withSchema(schema: String): PayloadBuilder = set(Schema, schema)

  override def build: Payload = Payload(fields)
}

object PayloadBuilder {
  def apply(): PayloadBuilder = new PayloadBuilder()

  def apply(fields: Fields): PayloadBuilder = apply().copy(fields)
}
