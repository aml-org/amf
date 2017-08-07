package amf.model

import amf.model.builder.PayloadBuilder

import scala.scalajs.js.annotation.JSExportAll

/**
  * payloads js class
  */
@JSExportAll
case class Payload private[model] (private[amf] val payload: amf.domain.Payload) extends DomainElement {

  val mediaType: String = payload.mediaType

  val schema: String = payload.schema

  def toBuilder: PayloadBuilder = PayloadBuilder(payload.toBuilder)
}
