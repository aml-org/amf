package amf.model

import amf.model.builder.PayloadBuilder

/**
  * Payload jvm class
  */
case class Payload private[model] (private[amf] val payload: amf.domain.Payload) extends DomainElement {

  val mediaType: String = payload.mediaType

  val schema: String = payload.schema

  def toBuilder: PayloadBuilder = PayloadBuilder(payload.toBuilder)
}
