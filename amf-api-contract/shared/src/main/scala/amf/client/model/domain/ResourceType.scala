package amf.client.model.domain
import amf.client.convert.ApiClientConverters._
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.platform.model.document.BaseUnit
import amf.core.client.platform.model.domain.{AbstractDeclaration, DomainElement}
import amf.plugins.domain.apicontract.models.templates.{ResourceType => InternalResourceType}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

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
