package amf.model

import amf.model.builder.PayloadBuilder

/**
  * Payload jvm class
  */
case class Payload private[model] (private val payload: amf.domain.Payload) extends DomainElement {

  val mediaType: String = payload.mediaType

  val schema: String = payload.schema

  def toBuilder: PayloadBuilder = PayloadBuilder(payload.toBuilder)

  override def equals(other: Any): Boolean = other match {
    case that: Payload =>
      (that canEqual this) &&
        payload == that.payload
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Payload]

  override private[amf] def element: amf.domain.Payload = payload
}
