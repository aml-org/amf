package amf.model

/**
  * JVM Payload model class.
  */
case class Payload private[model] (private val payload: amf.domain.Payload) extends DomainElement {

  def this() = this(amf.domain.Payload())

  val mediaType: String = payload.mediaType
  val schema: Shape     = Shape(payload.schema)

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
