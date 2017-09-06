package amf.model

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Payload model class.
  */
@JSExportAll
case class Payload private[model] (private val payload: amf.domain.Payload) extends DomainElement {

  def this() = this(amf.domain.Payload())

  val mediaType: String = payload.mediaType
  val schema: Shape     = Shape(payload.schema)

  override def equals(other: Any): Boolean = other match {
    case that: Payload =>
      (that canEqual this) &&
        payload == that.payload
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Payload]

  override private[amf] def element: amf.domain.Payload = payload

  /** Set mediaType property of this [[Payload]]. */
  def withMediaType(mediaType: String): this.type = {
    payload.withMediaType(mediaType)
    this
  }

  def withObjectSchema(name: String): NodeShape =
    NodeShape(payload.withObjectSchema(name))

  def withScalarSchema(name: String): ScalarShape =
    ScalarShape(payload.withScalarSchema(name))
}
