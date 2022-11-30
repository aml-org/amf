package amf.apicontract.client.platform.model.domain

import amf.apicontract.client.scala.model.domain.{Payload => InternalPayload}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.Linkable
import amf.core.client.scala.model.BoolField
import amf.shapes.client.platform.model.domain.Example
import amf.shapes.client.platform.model.domain.operations.AbstractPayload

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/** Payload model class.
  */
@JSExportAll
case class Payload(override private[amf] val _internal: InternalPayload) extends AbstractPayload(_internal) {

  override def linkCopy(): Payload = _internal.linkCopy()

  @JSExportTopLevel("Payload")
  def this() = this(InternalPayload())
  def schemaMediaType: StrField       = _internal.schemaMediaType
  def examples: ClientList[Example]   = _internal.examples.asClient
  def encodings: ClientList[Encoding] = _internal.encodings.asClient

  def required: BoolField = _internal.required

  /** Set specific media type of schema. */
  def withSchemaMediaType(mediaType: String): this.type = {
    _internal.withSchemaMediaType(mediaType)
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
  /** Set Required property of this Payload */
  def withRequired(required: Boolean): this.type = {
    _internal.withRequired(required)
    this
  }

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
