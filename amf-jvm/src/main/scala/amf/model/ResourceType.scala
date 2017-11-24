package amf.model

import amf.plugins.domain.webapi.models.templates

case class ResourceType private[model] (private val resourceType: templates.ResourceType)
    extends AbstractDeclaration(resourceType) {
  def this() = this(templates.ResourceType())

  override private[amf] def element = resourceType

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: templates.ResourceType => ResourceType(l) })

  override def linkCopy(): DomainElement with Linkable = ResourceType(element.linkCopy())
}
