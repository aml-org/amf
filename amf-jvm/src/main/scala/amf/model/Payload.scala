package amf.model

/**
  * Payload jvm class
  */
case class Payload private[model] (private val payload: amf.domain.Payload) extends DomainElement {

  def this() = this(amf.domain.Payload())

  val mediaType: String = payload.mediaType
  val schema: String    = payload.schema

  override def equals(other: Any): Boolean = other match {
    case that: Payload =>
      (that canEqual this) &&
        payload == that.payload
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Payload]

  override private[amf] def element: amf.domain.Payload = payload

  def withMediaType(mediaType: String): this.type = {
    payload.withMediaType(mediaType)
    this
  }
  def withSchema(schema: String): this.type = {
    payload.withSchema(schema)
    this
  }
}
