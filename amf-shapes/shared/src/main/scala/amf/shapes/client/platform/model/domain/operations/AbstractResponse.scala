package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.operations.{AbstractResponse => InternalAbstractResponse}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait AbstractResponse extends DomainElement with NamedDomainElement with PlatformSecrets {

  override private[amf] val _internal: InternalAbstractResponse
  type PayloadType <: AbstractPayload

  // Cannot implement because PayloadType is abstract
  def payload: PayloadType
  def withPayload(payload: PayloadType): this.type

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
