package amf.model.domain

import amf.ProfileNames
import amf.model.document.BaseUnit
import amf.plugins.domain.webapi.models.templates

case class ResourceType(private[amf] val resourceType: templates.ResourceType)
    extends AbstractDeclaration(resourceType) {
  override def linkTarget: Option[DomainElement with Linkable] =
    resourceType.linkTarget.map({ case l: templates.ResourceType => ResourceType(l) })

  override def linkCopy(): DomainElement with Linkable = ResourceType(resourceType.linkCopy())

  def asEndpoint[T <: BaseUnit](unit: T, profile: String = ProfileNames.RAML): EndPoint =
    EndPoint(resourceType.asEndpoint(unit.element, profile))
}
