package amf.domain

import amf.builder.PayloadBuilder
import amf.metadata.domain.PayloadModel._

/**
  * Payload internal model.
  */
case class Payload(fields: Fields) extends DomainElement {
  override type T = Payload

  val mediaType: String = fields(MediaType)
  val schema: String    = fields(Schema)

  def canEqual(other: Any): Boolean = other.isInstanceOf[Payload]

  override def equals(other: Any): Boolean = other match {
    case that: Payload =>
      (that canEqual this) &&
        mediaType == that.mediaType &&
        schema == that.schema

    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(mediaType, schema)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Payload($mediaType, $schema)"

  override def toBuilder: PayloadBuilder = PayloadBuilder(fields)
}

object Payload {
  def apply(fields: Fields): Payload = new Payload(fields)
}
