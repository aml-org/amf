package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.{Payload => InternalPayload}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Payload model class.
  */
@JSExportAll
case class Payload(override private[amf] val _internal: InternalPayload)
    extends DomainElement
    with NamedDomainElement {

  @JSExportTopLevel("model.domain.Payload")
  def this() = this(InternalPayload())
  def name: StrField                  = _internal.name
  def mediaType: StrField             = _internal.mediaType
  def schemaMediaType: StrField       = _internal.schemaMediaType
  def schema: Shape                   = _internal.schema
  def examples: ClientList[Example]   = _internal.examples.asClient
  def encodings: ClientList[Encoding] = _internal.encodings.asClient

  /** Set name property of this Payload. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set mediaType property of this Payload. */
  def withMediaType(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }

  /** Set specific media type of schema. */
  def withSchemaMediaType(mediaType: String): this.type = {
    _internal.withSchemaMediaType(mediaType)
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
  def withEncodings(encoding: ClientList[Encoding]): this.type = {
    _internal.withEncodings(encoding.asInternal)
    this
  }

  def withObjectSchema(name: String): NodeShape = _internal.withObjectSchema(name)

  def withScalarSchema(name: String): ScalarShape = _internal.withScalarSchema(name)

  def withExample(name: String): Example = _internal.withExample(Some(name))

  def withEncoding(name: String): Encoding = _internal.withEncoding(name)

  @deprecated(message = "Use method 'encodings'", "4.1.3")
  def encoding: ClientList[Encoding] = _internal.encodings.asClient
  @deprecated(message = "Use method 'withEncodings'", "4.1.3")
  def withEncoding(encoding: ClientList[Encoding]): this.type = {
    _internal.withEncodings(encoding.asInternal)
    this
  }
}
