package amf.model.domain

import amf.plugins.domain.webapi.models

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS Payload model class.
  */
@JSExportAll
case class Payload private[model] (private val payload: models.Payload) extends DomainElement {

  def this() = this(models.Payload())

  def mediaType: String = payload.mediaType
  def schema: Shape     = Shape(payload.schema)

  override private[amf] def element: models.Payload = payload

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
