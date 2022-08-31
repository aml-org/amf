package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Linkable, NamedAmfObject, Shape}
import amf.shapes.client.scala.model.domain.operations.{AbstractPayload => InternalAbstractPayload}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.platform.model.domain.{ArrayShape, NodeShape, ScalarShape}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
abstract class AbstractPayload(override private[amf] val _internal: InternalAbstractPayload)
    extends DomainElement
    with NamedAmfObject
    with PlatformSecrets
    with Linkable {

  def schema: Shape       = _internal.schema
  def mediaType: StrField = _internal.mediaType

  def withObjectSchema(name: String): NodeShape   = _internal.withObjectSchema(name)
  def withScalarSchema(name: String): ScalarShape = _internal.withScalarSchema(name)
  def withArraySchema(name: String): ArrayShape   = _internal.withArraySchema(name)
  def withMediaType(mediaType: String): this.type = {
    _internal.withMediaType(mediaType)
    this
  }
  def withSchema(schema: Shape): this.type = {
    _internal.withSchema(schema)
    this
  }

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
