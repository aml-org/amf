package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.{Payload => InternalPayload}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Payload model class.
  */
@JSExportAll
case class Payload(override private[amf] val _internal: InternalPayload) extends DomainElement {

  @JSExportTopLevel("model.domain.Payload")
  def this() = this(InternalPayload())

  def mediaType: StrField = _internal.mediaType
  def schema: Shape       = _internal.schema

  /** Set mediaType property of this Payload. */
  def withMediaType(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }

  def withObjectSchema(name: String): NodeShape = _internal.withObjectSchema(name)

  def withScalarSchema(name: String): ScalarShape = _internal.withScalarSchema(name)
}
