package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, NamedDomainElement}
import amf.shapes.client.scala.model.domain.operations.{AbstractRequest => InternalAbstractRequest}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait AbstractRequest extends DomainElement with NamedDomainElement with PlatformSecrets {

  override private[amf] val _internal: InternalAbstractRequest
  type ParameterType <: AbstractParameter

  // Cannot implement because ParameterType is abstract
  def queryParameters: ClientList[ParameterType]
  def withQueryParameters(parameters: ClientList[ParameterType]): this.type
  def withQueryParameter(name: String): ParameterType
  private[amf] def buildQueryParameter: ParameterType

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
