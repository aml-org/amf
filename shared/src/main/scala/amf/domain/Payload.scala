package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.PayloadModel._

/**
  * Payload internal model.
  */
case class Payload(fields: Fields, annotations: Annotations) extends DomainElement {

  def mediaType: String = fields(MediaType)
  def schema: String    = fields(Schema)

  def withMediaType(mediaType: String): this.type = set(MediaType, mediaType)
  def withSchema(schema: String): this.type       = set(Schema, schema)

  override def adopted(parent: String): this.type = {
    val mediaType: Option[String] = fields.?(MediaType)
    withId(parent + "/" + mediaType.getOrElse("default"))
  }
}

object Payload {
  def apply(): Payload = apply(Annotations())

  def apply(ast: AMFAST): Payload = apply(Annotations(ast))

  def apply(annotations: Annotations): Payload = new Payload(Fields(), annotations)
}
