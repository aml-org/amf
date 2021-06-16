package amf.apicontract.client.platform.model.domain.templates

import amf.apicontract.client.platform.model.domain.EndPoint
import amf.apicontract.internal.convert.ApiClientConverters.ClientOption
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.platform.model.document.BaseUnit
import amf.core.client.platform.model.domain.{AbstractDeclaration, DomainElement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.apicontract.client.scala.model.domain.templates.{ResourceType => InternalResourceType}
import amf.apicontract.internal.convert.ApiClientConverters._

@JSExportAll
case class ResourceType(override private[amf] val _internal: InternalResourceType)
    extends AbstractDeclaration(_internal) {

  @JSExportTopLevel("model.domain.ResourceType")
  def this() = this(InternalResourceType())

  override def linkTarget: ClientOption[DomainElement] = _internal.linkTarget.asClient

  override def linkCopy(): ResourceType = _internal.linkCopy()

  // TODO: ARM Remove (TOMI)
  def asEndpoint[T <: BaseUnit](unit: T, profile: ProfileName = Raml10Profile): EndPoint =
    _internal.asEndpoint(unit._internal, profile)
}
