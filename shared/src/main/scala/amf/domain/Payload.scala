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
}

object Payload {
  def apply(): Payload = new Payload(Fields(), Annotations())

  def apply(ast: AMFAST): Payload = new Payload(Fields(), Annotations(ast))
}
