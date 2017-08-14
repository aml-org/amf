package amf.builder

import amf.domain.{Annotation, Fields, Payload}
import amf.metadata.domain.PayloadModel._

/**
  * Payload domain element builder.
  */
class PayloadBuilder extends Builder {
  override type T = Payload

  def withMediaType(mediaType: String): PayloadBuilder = set(MediaType, mediaType)

  def withSchema(schema: String): PayloadBuilder = set(Schema, schema)

  override def resolveId(container: String): this.type = {
    val mediaType: Option[String] = fields.?(MediaType)
    withId(container + "/" + mediaType.getOrElse("default"))
  }

  override def build: Payload = Payload(fields, annotations)
}

object PayloadBuilder {
  def apply(): PayloadBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): PayloadBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): PayloadBuilder = new PayloadBuilder().withAnnotations(annotations)
}
