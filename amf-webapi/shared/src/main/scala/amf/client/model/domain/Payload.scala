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

  def mediaType: StrField            = _internal.mediaType
  def schema: Shape                  = _internal.schema
  def examples: ClientList[Example]  = _internal.examples.asClient
  def encoding: ClientList[Encoding] = _internal.encoding.asClient

  /** Set mediaType property of this Payload. */
  def withMediaType(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }

  /** Set schema property of this Payload. */
  def withSchema(schema: Shape): this.type = {
    _internal.withSchema(schema)
    this
  }

  /** Set examples property of this Payload. */
  def withExamples(examples: ClientList[Example]): this.type = {
    _internal.withExamples(examples.asInternal)
    this
  }

  /** Set encoding property of this Payload. */
  def withEncoding(encoding: ClientList[Encoding]): this.type = {
    _internal.withEncoding(encoding.asInternal)
    this
  }

  def withObjectSchema(name: String): NodeShape = _internal.withObjectSchema(name)

  def withScalarSchema(name: String): ScalarShape = _internal.withScalarSchema(name)

  def withExample(name: String): Example = _internal.withExample(Some(name))

  def withEncoding(name: String): Encoding = _internal.withEncoding(name)
}
